package com.bresiu.testfield;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.TextInputLayout;
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
import java.util.regex.Pattern;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends RxAppCompatActivity {

	@Bind(R.id.input_text_repository) EditText inputTextRepository;
	@Bind(R.id.simple_button) Button simpleButton;
	@Bind(R.id.emailInputLayout) TextInputLayout emailInputLayout;
	@Bind(R.id.passwordInputLayout) TextInputLayout passwordInputLayout;
	private EditText email;
	private EditText password;

	private Random random;

	ObservableList<Integer> observableList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.random = new Random();
		this.observableList = new ObservableList<>();
		this.bindDataToLayout();
		ButterKnife.bind(this);
		this.email = emailInputLayout.getEditText();
		this.password = passwordInputLayout.getEditText();
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
		this.observableList.getObservable().subscribe();
	}

	private void testThread() {
		Observable.just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
			.compose(bindToLifecycle())
			.filter(this::heavyCalculations)
			.compose(applySchedulers())
			.subscribe();
	}

	private Boolean heavyCalculations(Integer integer) {
		Log.d("TROL", Thread.currentThread().getName());
		return true;
	}

	private void setupEditTextListeners() {
		final Pattern emailPattern = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

		Observable<Boolean> emailValid = RxTextView.textChangeEvents(email)
			.skip(1)
			.map(TextViewTextChangeEvent::text)
			.map(t -> emailPattern.matcher(t).matches() || t.length() == 0);

		Observable<Boolean> passwordValid = RxTextView.textChangeEvents(password)
			.skip(1)
			.map(TextViewTextChangeEvent::text)
			.map(t -> t.length() > 4 || t.length() == 0);

		emailValid.debounce(1000, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
			.distinctUntilChanged()
			.doOnNext(b -> {
				Log.d("TROL", "Email " + (b ? "Valid" : "Invalid"));
				if (b) {
					emailInputLayout.setError(null);
				} else {
					emailInputLayout.setError("invalid email");
				}
			})
			.map(b -> b ? Color.WHITE : Color.GRAY)
			.subscribe(email::setTextColor);

		passwordValid.debounce(1000, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
			.distinctUntilChanged()
			.doOnNext(b -> {
				Log.d("TROL", "Password " + (b ? "Valid" : "Invalid"));
				if (b) {
					passwordInputLayout.setError(null);
				} else {
					passwordInputLayout.setError("invalid password");
				}
			})
			.map(b -> b ? Color.WHITE : Color.GRAY)
			.subscribe(password::setTextColor);

		Observable<Boolean> registerEnabled =
			Observable.combineLatest(emailValid, passwordValid, (a, b) -> a && b);
		registerEnabled.distinctUntilChanged()
			.doOnNext(b -> Log.d("TROL", "Button " + (b ? "Enabled" : "Disabled")))
			.subscribe(simpleButton::setEnabled);
	}

	private void doSomeJob() {
		RxView.clicks(this.simpleButton).subscribe(o -> Log.d("TROL", "simple button clicked!"));
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
				Log.d("TROL", "onCompleted");
			}

			@Override
			public void onError(Throwable e) {
				Log.e("TROL", e.getMessage());
			}

			@Override
			public void onNext(TextViewTextChangeEvent textViewTextChangeEvent) {
				Log.d("TROL", textViewTextChangeEvent.text().toString());
			}
		};
	}

	private void logCurrentThread() {
		if (this.isCurrentlyOnMainThread()) {
			Log.d("TROL", "MAIN THREAD:");
		} else {
			Log.d("TROL", "NOT MAIN THREAD:");
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
