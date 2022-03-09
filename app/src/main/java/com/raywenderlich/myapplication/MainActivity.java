package com.raywenderlich.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.raywenderlich.myapplication.adapter.PersonAdapter;
import com.raywenderlich.myapplication.model.Person;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements PresenterInterface {

    private TextView textViewResult, textViewRx_result;
    private Button callApiBtn, singleBtn;
    private RecyclerView recyclerViewPerson;
    private PersonAdapter personAdapter;
    private Presenter myPresenter;
    private CompositeDisposable compositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewResult = findViewById(R.id.text_view_result);
        textViewRx_result = findViewById(R.id.rx_result_tv);
        callApiBtn = findViewById(R.id.callapi);
        singleBtn = findViewById(R.id.rx_single);
        callApiBtn.setOnClickListener(view -> {
            myPresenter.callAPI_Get_ListPost();
        });

        singleBtn.setOnClickListener(view -> {
            saveDatabase_RxSingle();
        });

        myPresenter = new Presenter(this);
        compositeDisposable = new CompositeDisposable();

        recyclerViewPerson = (RecyclerView) findViewById(R.id.recycleview_person);
        recyclerViewPerson.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPerson.setItemAnimator(new DefaultItemAnimator());

        bindData();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
        Log.e("hahaha", "Dispose");
    }

    @Override
    public void onCallAPI_Get_ListPost(int type, String result) {
        switch (type) {
            case -1:
                textViewResult.setText("Error" + "\n" + result);
                break;
            default:
                textViewResult.setText(result);
                break;
        }

    }

    private void saveDatabase_RxSingle() {
        myPresenter.callAPI_Get_SaveDatabase_Rx_Single()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> {
                    Log.e("hahaha", "Do onSubscribe " + Thread.currentThread().getName()); //Do onSubscribe main

                })
                .subscribe(new SingleObserver<String>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        compositeDisposable.add(d);
                        singleBtn.setEnabled(false);
                        Log.e("hahaha", "onSubscribe " + Thread.currentThread().getName());//onSubscribe main
                        textViewRx_result.setText("onSubscribe " + Thread.currentThread().getName());
                    }

                    @Override
                    public void onSuccess(@NonNull String s) {
                        Log.e("hahaha", "onSuccess " + Thread.currentThread().getName());//onSuccess main
                        textViewRx_result.setText("onSuccess " + Thread.currentThread().getName());
                        singleBtn.setEnabled(true);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e("hahaha", "onError " + Thread.currentThread().getName());
                        textViewRx_result.setText("onError " + Thread.currentThread().getName());
                        singleBtn.setEnabled(true);
                    }
                });
    }

    private void bindData() {
        getDatabase_ListPerson();
    }

    private void getDatabase_ListPerson() {
        if (myPresenter.getPersonList().size() == 0)
            myPresenter.initDataPersonList()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CompletableObserver() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {
                            compositeDisposable.add(d);
                            textViewRx_result.setText("Get database");
                        }

                        @Override
                        public void onComplete() {
                            Log.e("hahaha", "Xu ly: getDatabase() 3");
                            textViewRx_result.setText(myPresenter.getRxText());
                            setUpAdapterPerson(myPresenter.getPersonList());
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            textViewRx_result.setText(e.getMessage());
                        }
                    });
    }

    private void setUpAdapterPerson(List<Person> personList) {
        personAdapter = new PersonAdapter(personList, this);
        recyclerViewPerson.setAdapter(personAdapter);
    }
}