package com.bresiu.testfield;

import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by Bresiu on 14-09-2015
 */
public class ObservableList<T> {
	protected final List<T> list;
	protected final PublishSubject<T> onAdd;

	public ObservableList() {
		this.list = new ArrayList<>();
		this.onAdd = PublishSubject.create();
	}

	public void add(T value) {
		list.add(value);
		onAdd.onNext(value);
	}

	public Observable<T> getObservable() {
		return onAdd;
	}
}
