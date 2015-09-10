package com.bresiu.testfield;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.bresiu.testfield.databinding.ActivityMainBinding;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewTextChangeEvent;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

	@Bind(R.id.input_text_repository) EditText inputTextTepository;

	private Random random;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.random = new Random();
		this.bindDataToLayout();
		ButterKnife.bind(this);
		this.initRepositorySearch();
	}

	private void initRepositorySearch() {
		RxTextView.textChangeEvents(this.inputTextTepository)
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
}
