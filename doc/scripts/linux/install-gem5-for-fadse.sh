#!/usr/bin/env bash
set -euo pipefail

usage() {
    cat <<'USAGE'
Usage: install-gem5-for-fadse.sh [options]

Downloads, builds, and smoke-tests gem5 for the FADSE gem5 connector.

Options:
  --version VERSION        gem5 tag to build. Default: v25.1.0.1
  --isa ISA                gem5 ISA build target. Default: X86
  --variant VARIANT        gem5 binary variant. Default: opt
  --jobs N                SCons parallel jobs. Default: min(nproc, 4)
  --src-root DIR          Parent directory for gem5 sources. Default: ~/.local/src
  --link DIR              Stable symlink to the selected gem5 tree. Default: ~/.local/gem5-fadse
  --config PATH           FADSE XML config to generate. Default: configs/designSpace/gem5_local.xml
  --no-write-config       Do not generate a local FADSE XML config.
  --no-smoke-test         Skip the /bin/true gem5 smoke test.
  --install-deps          Install common Ubuntu/Debian build dependencies with apt.
  -h, --help              Show this help.

Environment variables with the same names are also supported:
  GEM5_VERSION, GEM5_ISA, GEM5_VARIANT, GEM5_BUILD_JOBS, GEM5_SRC_ROOT,
  GEM5_LINK, FADSE_CONFIG_PATH.
USAGE
}

default_jobs() {
    if command -v nproc >/dev/null 2>&1; then
        local cpus
        cpus="$(nproc)"
        if [ "$cpus" -gt 4 ]; then
            echo 4
        else
            echo "$cpus"
        fi
    else
        echo 2
    fi
}

die() {
    echo "error: $*" >&2
    exit 1
}

install_deps() {
    command -v apt-get >/dev/null 2>&1 || die "--install-deps currently supports apt-based Linux only"

    sudo apt-get update
    sudo apt-get install -y \
        build-essential git m4 scons zlib1g zlib1g-dev \
        python3 python3-dev python3-venv python3-tk pkg-config \
        libprotobuf-dev protobuf-compiler libprotoc-dev \
        libgoogle-perftools-dev libboost-all-dev \
        libhdf5-serial-dev libcapstone-dev libpng-dev libelf-dev \
        wget cmake
}

require_commands() {
    local missing=()
    local command_name

    for command_name in git python3 gcc g++ m4; do
        if ! command -v "$command_name" >/dev/null 2>&1; then
            missing+=("$command_name")
        fi
    done

    if [ "${#missing[@]}" -gt 0 ]; then
        die "missing required command(s): ${missing[*]}. Re-run with --install-deps or install them manually."
    fi
}

ensure_venv() {
    local venv_dir="$1"

    if [ ! -x "$venv_dir/bin/python" ]; then
        python3 -m venv "$venv_dir" || die "could not create Python venv. Install python3-venv and retry."
    fi

    "$venv_dir/bin/python" -m pip install --upgrade pip setuptools wheel scons
}

checkout_gem5() {
    local version="$1"
    local src_dir="$2"
    local src_root="$3"

    mkdir -p "$src_root"

    if [ -d "$src_dir/.git" ]; then
        echo "Using existing git checkout: $src_dir"
        git -C "$src_dir" fetch --tags --depth 1 origin "$version" || git -C "$src_dir" fetch --tags origin
        git -C "$src_dir" checkout "$version"
    elif [ -f "$src_dir/SConstruct" ]; then
        echo "Using existing gem5 source tree: $src_dir"
    elif [ -e "$src_dir" ]; then
        die "$src_dir exists but does not look like a gem5 source tree"
    else
        echo "Cloning gem5 $version into $src_dir"
        git clone --branch "$version" --depth 1 https://github.com/gem5/gem5.git "$src_dir"
    fi
}

create_link() {
    local src_dir="$1"
    local link_dir="$2"

    mkdir -p "$(dirname "$link_dir")"
    if [ -L "$link_dir" ] || [ ! -e "$link_dir" ]; then
        ln -sfn "$src_dir" "$link_dir"
        echo "Stable gem5 path: $link_dir"
    else
        echo "Not replacing existing non-symlink path: $link_dir"
    fi
}

xml_escape() {
    printf '%s' "$1" \
        | sed -e 's/&/\&amp;/g' \
              -e 's/"/\&quot;/g' \
              -e "s/'/\&apos;/g" \
              -e 's/</\&lt;/g' \
              -e 's/>/\&gt;/g'
}

write_fadse_config() {
    local config_path="$1"
    local executable="$2"
    local config_script="$3"

    mkdir -p "$(dirname "$config_path")"

    cat > "$config_path" <<XML
<?xml version="1.0" encoding="UTF-8"?>

<design_space>
    <problem_config name="gem5" type="simulator">
        <parameter name="realSimulator" value="gem5"/>
        <parameter name="gem5_executable" value="$(xml_escape "$executable")"/>
        <parameter name="gem5_config_script" value="$(xml_escape "$config_script")"/>
        <parameter name="gem5_output_dir" value="/tmp/fadse-gem5-#"/>
        <parameter name="gem5_stats_file" value="stats.txt"/>
    </problem_config>
    <parameters>
        <parameter name="num-cpus" description="gem5 se.py option" type="integer" min="1" max="2"/>
    </parameters>
    <benchmarks>
        <item name="/bin/true"/>
    </benchmarks>
    <metaheuristic name="NSGAII" config_path="nsga2.json"/>
    <objectives>
        <objective name="simTicks" type="float" unit="ticks" desired="small"/>
        <objective name="hostSeconds" type="float" unit="seconds" desired="small"/>
    </objectives>
    <database ip="127.0.0.1" port="3306" name="fadse" user="root" password="root"/>
    <clients fileName="clientsConfig.xml"/>
    <output path="/tmp/fadse-gem5-output"/>
</design_space>
XML

    echo "Generated FADSE gem5 config: $config_path"
}

run_smoke_test() {
    local binary="$1"
    local src_dir="$2"
    local isa="$3"
    local config_script="$src_dir/configs/deprecated/example/se.py"
    local smoke_dir

    if [ "$isa" != "X86" ] && [ "$isa" != "ALL" ]; then
        echo "Skipping smoke test: default /bin/true test is for X86/ALL builds"
        return
    fi

    [ -f "$config_script" ] || die "smoke-test config script not found: $config_script"
    [ -x /bin/true ] || die "smoke-test benchmark not executable: /bin/true"

    smoke_dir="$(mktemp -d "${TMPDIR:-/tmp}/fadse-gem5-smoke.XXXXXX")"
    "$binary" --outdir="$smoke_dir" --stats-file=stats.txt "$config_script" --cmd=/bin/true
    grep -Eq '^simTicks[[:space:]]+' "$smoke_dir/stats.txt" || die "simTicks not found in $smoke_dir/stats.txt"
    grep -Eq '^simInsts[[:space:]]+' "$smoke_dir/stats.txt" || die "simInsts not found in $smoke_dir/stats.txt"

    echo "gem5 smoke test passed: $smoke_dir/stats.txt"
}

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"

GEM5_VERSION="${GEM5_VERSION:-v25.1.0.1}"
GEM5_ISA="${GEM5_ISA:-X86}"
GEM5_VARIANT="${GEM5_VARIANT:-opt}"
GEM5_BUILD_JOBS="${GEM5_BUILD_JOBS:-$(default_jobs)}"
GEM5_SRC_ROOT="${GEM5_SRC_ROOT:-$HOME/.local/src}"
GEM5_LINK="${GEM5_LINK:-$HOME/.local/gem5-fadse}"
FADSE_CONFIG_PATH="${FADSE_CONFIG_PATH:-$REPO_ROOT/configs/designSpace/gem5_local.xml}"
INSTALL_DEPS=0
RUN_SMOKE_TEST=1
WRITE_CONFIG=1

while [ "$#" -gt 0 ]; do
    case "$1" in
        --version)
            GEM5_VERSION="${2:?missing value for --version}"
            shift 2
            ;;
        --isa)
            GEM5_ISA="${2:?missing value for --isa}"
            shift 2
            ;;
        --variant)
            GEM5_VARIANT="${2:?missing value for --variant}"
            shift 2
            ;;
        --jobs)
            GEM5_BUILD_JOBS="${2:?missing value for --jobs}"
            shift 2
            ;;
        --src-root)
            GEM5_SRC_ROOT="${2:?missing value for --src-root}"
            shift 2
            ;;
        --link)
            GEM5_LINK="${2:?missing value for --link}"
            shift 2
            ;;
        --config)
            FADSE_CONFIG_PATH="${2:?missing value for --config}"
            WRITE_CONFIG=1
            shift 2
            ;;
        --install-deps)
            INSTALL_DEPS=1
            shift
            ;;
        --no-smoke-test)
            RUN_SMOKE_TEST=0
            shift
            ;;
        --no-write-config)
            WRITE_CONFIG=0
            shift
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            usage >&2
            die "unknown option: $1"
            ;;
    esac
done

[[ "$GEM5_BUILD_JOBS" =~ ^[0-9]+$ ]] || die "--jobs must be a positive integer"
[ "$GEM5_BUILD_JOBS" -gt 0 ] || die "--jobs must be greater than zero"

if [ "$INSTALL_DEPS" -eq 1 ]; then
    install_deps
fi

require_commands

VERSION_WITHOUT_V="${GEM5_VERSION#v}"
GEM5_SRC_DIR="$GEM5_SRC_ROOT/gem5-$VERSION_WITHOUT_V"
VENV_DIR="$GEM5_SRC_ROOT/.venvs/gem5-build"
BUILD_TARGET="build/$GEM5_ISA/gem5.$GEM5_VARIANT"
GEM5_BINARY="$GEM5_SRC_DIR/$BUILD_TARGET"
SMOKE_CONFIG_SCRIPT="$GEM5_SRC_DIR/configs/deprecated/example/se.py"

checkout_gem5 "$GEM5_VERSION" "$GEM5_SRC_DIR" "$GEM5_SRC_ROOT"
ensure_venv "$VENV_DIR"

echo "Building gem5 target: $BUILD_TARGET with -j$GEM5_BUILD_JOBS"
(
    cd "$GEM5_SRC_DIR"
    "$VENV_DIR/bin/scons" "$BUILD_TARGET" -j "$GEM5_BUILD_JOBS"
)

[ -x "$GEM5_BINARY" ] || die "gem5 binary was not produced: $GEM5_BINARY"

create_link "$GEM5_SRC_DIR" "$GEM5_LINK"

if [ "$RUN_SMOKE_TEST" -eq 1 ]; then
    run_smoke_test "$GEM5_BINARY" "$GEM5_SRC_DIR" "$GEM5_ISA"
fi

if [ "$WRITE_CONFIG" -eq 1 ]; then
    write_fadse_config "$FADSE_CONFIG_PATH" "$GEM5_BINARY" "$SMOKE_CONFIG_SCRIPT"
fi

cat <<EOF

gem5 is ready for FADSE.

gem5 source:      $GEM5_SRC_DIR
gem5 executable:  $GEM5_BINARY
FADSE XML config: ${FADSE_CONFIG_PATH:-not generated}

Optional connector verification:
  mvn -Dtest=core.network.client.gem5.Gem5IntegrationTest \\
    -Dgem5.executable=$GEM5_BINARY \\
    -Dgem5.configScript=$SMOKE_CONFIG_SCRIPT \\
    -Dgem5.benchmark=/bin/true \\
    test
EOF
