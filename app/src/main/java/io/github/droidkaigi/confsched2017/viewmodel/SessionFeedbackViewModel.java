package io.github.droidkaigi.confsched2017.viewmodel;

import com.android.databinding.library.baseAdapters.BR;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.view.View;

import java.util.Locale;

import javax.inject.Inject;

import io.github.droidkaigi.confsched2017.model.Session;
import io.github.droidkaigi.confsched2017.model.SessionFeedback;
import io.github.droidkaigi.confsched2017.repository.feedbacks.SessionFeedbackRepository;
import io.github.droidkaigi.confsched2017.repository.sessions.SessionsRepository;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;


public final class SessionFeedbackViewModel extends BaseObservable implements ViewModel {

    private static final String TAG = SessionFeedbackViewModel.class.getSimpleName();

    private final SessionsRepository sessionsRepository;

    private final SessionFeedbackRepository sessionFeedbackRepository;

    private final CompositeDisposable compositeDisposable;

    public Session session;

    private String sessionTitle;

    private int relevancy;

    private int asExpected;

    private int difficulty;

    private int knowledgeable;

    private String comment;

    private Callback callback;

    @Inject
    SessionFeedbackViewModel(SessionsRepository sessionsRepository,
            SessionFeedbackRepository sessionFeedbackRepository,
            CompositeDisposable compositeDisposable) {
        this.sessionsRepository = sessionsRepository;
        this.sessionFeedbackRepository = sessionFeedbackRepository;
        this.compositeDisposable = compositeDisposable;
    }

    public void findSession(int sessionId) {
        Disposable disposable = sessionsRepository.find(sessionId, Locale.getDefault())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::setSession,
                        throwable -> Timber.tag(TAG).e(throwable, "Failed to find session.")
                );
        compositeDisposable.add(disposable);
    }

    private void setSession(@NonNull Session session) {
        this.session = session;
        this.sessionTitle = session.title;
        notifyPropertyChanged(BR.sessionTitle);
    }

    @Override
    public void destroy() {
        compositeDisposable.clear();
        callback = null;
    }

    @Bindable
    public String getSessionTitle() {
        return sessionTitle;
    }

    public void setSessionTitle(String sessionTitle) {
        this.sessionTitle = sessionTitle;
        notifyPropertyChanged(BR.sessionTitle);
    }

    public void onClickSubmitFeedbackButton(@SuppressWarnings("unused") View view) {
        SessionFeedback sessionFeedback =
                new SessionFeedback(session, relevancy, asExpected, difficulty, knowledgeable, comment);
        compositeDisposable.add(sessionFeedbackRepository.submit(sessionFeedback)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    if (callback != null) {
                        callback.onSuccessSubmit();
                    }
                }, failure -> {
                    if (callback != null) {
                        callback.onErrorSubmit();
                    }
                }));
    }

    @Bindable
    public int getRelevancy() {
        return relevancy;
    }

    public void setRelevancy(int relevancy) {
        this.relevancy = relevancy;
        notifyPropertyChanged(BR.relevancy);
    }

    @Bindable
    public int getAsExpected() {
        return asExpected;
    }

    public void setAsExpected(int asExpected) {
        this.asExpected = asExpected;
        notifyPropertyChanged(BR.asExpected);
    }

    @Bindable
    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
        notifyPropertyChanged(BR.difficulty);
    }

    @Bindable
    public int getKnowledgeable() {
        return knowledgeable;
    }

    public void setKnowledgeable(int knowledgeable) {
        this.knowledgeable = knowledgeable;
        notifyPropertyChanged(BR.knowledgeable);
    }

    @Bindable
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
        notifyPropertyChanged(BR.comment);
    }

    public void setCallback(@NonNull Callback callback) {
        this.callback = callback;
    }

    public interface Callback {

        void onSuccessSubmit();

        void onErrorSubmit();
    }
}
