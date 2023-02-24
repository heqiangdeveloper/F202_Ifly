package com.iflytek.adapter.controllerInterface;

public interface IController<T,V,Z> {
    void srAction(T t);

    void mvwAction(V v);

    void stkAction(Z z);

}
