package jmetal.core.util.observable.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import jmetal.core.util.JMetalLogger;
import jmetal.core.util.observable.Observable;
import jmetal.core.util.observer.Observer;

/** @author Antonio J. Nebro <antonio@lcc.uma.es> */
public class DefaultObservable<D> implements Observable<D> {
  private Set<Observer<D>> observers;
  private boolean dataHasChanged;
  private String name;

  public DefaultObservable(String name) {
    observers = new HashSet<>();
    dataHasChanged = false;
    this.name = name;
  }

  @Override
  public synchronized void register(Observer<D> observer) {
    observers.add(observer);
    JMetalLogger.logger.info("DefaultObservable " + name + ": " + observer + " registered");
  }

  @Override
  public synchronized void unregister(Observer<D> observer) {
    observers.remove(observer);
  }

  @Override
  public synchronized void notifyObservers(D data) {
    if (dataHasChanged) {
      observers.forEach(observer -> observer.update(this, data));
    }
    clearChanged();
  }

  @Override
  public synchronized int numberOfRegisteredObservers() {
    return observers.size();
  }

  @Override
  public synchronized void setChanged() {
    dataHasChanged = true;
  }

  @Override
  public synchronized boolean hasChanged() {
    return dataHasChanged;
  }

  @Override
  public synchronized void clearChanged() {
    dataHasChanged = false;
  }

  public synchronized String getName() {
    return name;
  }

  public synchronized void setName(String name) {
    this.name = name;
  }

  @Override
  public Collection<Observer<D>> observers() {
    return observers;
  }
}
