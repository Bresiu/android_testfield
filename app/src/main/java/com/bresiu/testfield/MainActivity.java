package com.bresiu.testfield;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.bresiu.testfield.databinding.ActivityMainBinding;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewTextChangeEvent;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends RxAppCompatActivity {

	@Bind(R.id.input_text_repository) EditText inputTextRepository;
	@Bind(R.id.simple_button) Button simpleButton;
	@Bind(R.id.email) EditText email;
	@Bind(R.id.password) EditText password;

	private Random random;

	ObservableList<Integer> observableList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.random = new Random();
		this.observableList = new ObservableList<>();
		this.bindDataToLayout();
		ButterKnife.bind(this);
		this.initRepositorySearch();
		this.doSomeJob();
		this.testThread();
		this.setupEditTextListeners();
		try {
			this.bindArrayList();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void bindArrayList() throws InterruptedException {
		this.observableList.getObservable().subscribe(i -> Log.d("TROL", "int: " + i));
	}

	private void saveToDataBase(String user) {
		Log.d("TEST", user);
	}

	private void testThread() {
		Observable.just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
			.compose(bindToLifecycle())
			.filter(this::heavyCalculations)
			.compose(applySchedulers())
			.subscribe();
	}

	private Boolean heavyCalculations(Integer integer) {
		Log.d("TEST", Thread.currentThread().getName());
		return true;
	}

	private void setupEditTextListeners() {
		Observable<TextViewTextChangeEvent> emailChangeObservable =
			RxTextView.textChangeEvents(this.email);
		Observable<TextViewTextChangeEvent> passwordChangeObservable =
			RxTextView.textChangeEvents(this.password);

		RxView.enabled(this.simpleButton).call(false);

		Observable.combineLatest(emailChangeObservable, passwordChangeObservable,
			(emailObservable, passwordObservable) -> {
				boolean emailCheck = this.checkEmailField(emailObservable.text().toString());
				boolean passwordCheck = passwordObservable.text().length() >= 3;

				return emailCheck && passwordCheck;
			}).subscribe(aBoolean -> RxView.enabled(this.simpleButton).call(aBoolean));
	}

	private boolean checkEmailField(String sequence) {
		boolean length;
		boolean at;
		Log.d("TEST", "sequence: " + sequence);

		if (sequence.length() >= 3) {
			length = true;
		} else {
			this.email.setError("email length should be more than 3 characters");
			length = false;
		}
		if (sequence.contains("@")) {
			at = true;
		} else {
			at = false;
			this.email.setError("email should contains @");
		}
		return length && at;
	}

	private void doSomeJob() {
		RxView.clicks(this.simpleButton).subscribe(o -> Log.d("TEST", "simple button clicked!"));
	}

	private void initRepositorySearch() {
		RxTextView.textChangeEvents(this.inputTextRepository)
			.compose(bindToLifecycle())
			.filter(textChangeEvent -> textChangeEvent.text().length() >= 3)
			.debounce(500, TimeUnit.MILLISECONDS)
			.observeOn(AndroidSchedulers.mainThread())
			.doOnEach(notification -> MainActivity.this.logCurrentThread())
			.subscribe(this.getSearchObserver());
	}

	private Observer<TextViewTextChangeEvent> getSearchObserver() {
		return new Observer<TextViewTextChangeEvent>() {
			@Override
			public void onCompleted() {
				Log.d("TEST", "onCompleted");
			}

			@Override
			public void onError(Throwable e) {
				Log.e("TEST", e.getMessage());
			}

			@Override
			public void onNext(TextViewTextChangeEvent textViewTextChangeEvent) {
				Log.d("TEST", textViewTextChangeEvent.text().toString());
			}
		};
	}

	private void logCurrentThread() {
		if (this.isCurrentlyOnMainThread()) {
			Log.d("TEST", "MAIN THREAD:");
		} else {
			Log.d("TEST", "NOT MAIN THREAD:");
		}
	}

	private boolean isCurrentlyOnMainThread() {
		return Looper.myLooper() == Looper.getMainLooper();
	}

	private void bindDataToLayout() {
		ActivityMainBinding bindings = DataBindingUtil.setContentView(this, R.layout.activity_main);
		User user = new User();
		MyHandlers handlers = new MyHandlers();
		bindings.setUser(user);
		bindings.setHandlers(handlers);

		Observable.interval(1, 3, TimeUnit.SECONDS, Schedulers.newThread()).subscribe((ignored) -> {
			int randomNumber = this.getRandomInt();
			user.setNumber(randomNumber);
			this.observableList.add(randomNumber);
			String randomString = this.getRandomString();
			user.setUsername(randomString);
		});
	}

	private int getRandomInt() {
		return this.random.nextInt();
	}

	private String getRandomString() {
		return UUID.randomUUID().toString();
	}

	private <T> Observable.Transformer<T, T> applySchedulers() {
		return observable -> observable.subscribeOn(Schedulers.io())
			.observeOn(AndroidSchedulers.mainThread());
	}
}
