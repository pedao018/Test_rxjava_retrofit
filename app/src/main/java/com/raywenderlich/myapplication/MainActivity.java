package com.raywenderlich.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.raywenderlich.myapplication.adapter.PersonAdapter;
import com.raywenderlich.myapplication.model.Person;
import com.raywenderlich.myapplication.model.Post;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class MainActivity extends AppCompatActivity implements PresenterInterface {

    private TextView textViewResult, textViewRx_result;
    private Button callApiBtn, singleBtn, callRxApiBtn, callMutilRxApiBtn;
    private EditText edtSearch;
    private RecyclerView recyclerViewPerson;
    private PersonAdapter personAdapter;
    private Presenter myPresenter;
    private CompositeDisposable compositeDisposable;

    private int seach_turnApiCall = 0;
    private int seach_result = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewResult = findViewById(R.id.text_view_result);
        textViewRx_result = findViewById(R.id.rx_result_tv);
        callApiBtn = findViewById(R.id.callapi);
        singleBtn = findViewById(R.id.rx_single);
        callRxApiBtn = findViewById(R.id.call_rxapi);
        callMutilRxApiBtn = findViewById(R.id.call_mutilrxapi);
        callApiBtn.setOnClickListener(view -> {
            myPresenter.callAPI_Get_ListPost();
        });
        callRxApiBtn.setOnClickListener(view -> {
            getRxApi_1();
        });

        callMutilRxApiBtn.setOnClickListener(view -> {
            getMutilRxApi_AndSaveDatabase_1();
        });

        singleBtn.setOnClickListener(view -> {
            saveDatabase_RxSingle();
        });
        edtSearch = findViewById(R.id.edtSearch);
        searchViewObserable(edtSearch)
                .debounce(1000, TimeUnit.MILLISECONDS)
                .filter(s -> {
                    if (!s.trim().isEmpty()) return true;
                    return false;
                })
                .distinctUntilChanged()
                .switchMap(s -> {
                    Log.e("hahaha", s + " " + Thread.currentThread().getName());
                    return Observable.just(searchAPI(s)).delay(5000, TimeUnit.MILLISECONDS);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull String s) {
                        seach_result++;
                        Log.e("hahaha", seach_result + " " + Thread.currentThread().getName());
                        textViewRx_result.setText(s);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

        myPresenter = new Presenter(this);
        compositeDisposable = new CompositeDisposable();

        recyclerViewPerson = (RecyclerView) findViewById(R.id.recycleview_person);
        recyclerViewPerson.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPerson.setItemAnimator(new DefaultItemAnimator());

        //bindData();
        Log.e("hahaha", "In ra");
    }

    private String searchAPI(String s) {
        String result = "";
        try {
            //myPresenter.callAPI_Search_ListPost();
            seach_turnApiCall++;
            result = seach_turnApiCall + " - " + s;
            Log.e("hahaha", "Search " + Thread.currentThread().getName());
        } catch (Exception e) {
            return e.getMessage();
        }
        return result;
    }

    private Observable<String> searchViewObserable(EditText view) {
        PublishSubject<String> subject = PublishSubject.create();
        view.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence != null) {
                    subject.onNext(charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        return subject;
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
        myPresenter.saveDatabase_Rx_Single()
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

    private void getRxApi_1() {
        myPresenter.getRxApi_1()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Post>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        compositeDisposable.add(d);
                        Log.e("hahaha", "Xu ly: getRxApi_1() onSubscribe");
                        textViewRx_result.setText("onSubscribe Get getRxApi_1");
                    }

                    @Override
                    public void onNext(@NonNull List<Post> posts) {
                        Log.e("hahaha", "Xu ly: getRxApi_1() onNext");
                        textViewRx_result.setText("onNext Get getRxApi_1");
                        String result = myPresenter.handleResult(posts);
                        textViewResult.setText(result);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e("hahaha", "Xu ly: getRxApi_1() onError");
                        textViewRx_result.setText("Error Get getRxApi_1");
                    }

                    @Override
                    public void onComplete() {
                        Log.e("hahaha", "Xu ly: getRxApi_1() onComplete");
                        textViewRx_result.setText("Complete Get getRxApi_1");
                    }
                });
    }

    //Goi song song API dung RxAndroid
    private void getMutilRxApi_1() {
        Observable<List<String>> api1 = myPresenter.getRxApi_1()
                .map(posts -> myPresenter.handleResult(posts)).toList().toObservable();
        Observable<List<Post>> api2 = myPresenter.getRxApi_1();
        Observable<List<Post>> api3 = myPresenter.getRxApi_1();
        Observable.zip(api1, api2, api3, (api1Datas, api2Datas, api3Datas) -> {
            return new RxApi_1(api1Datas, api2Datas, api3Datas);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<RxApi_1>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        compositeDisposable.add(d);
                        textViewResult.setText("");
                        recyclerViewPerson.setAdapter(null);
                        Log.e("hahaha", "Xu ly: getRxApi_1() onSubscribe");
                    }

                    @Override
                    public void onNext(@NonNull RxApi_1 rxApi_1) {
                        saveDatabase_RxSingle();
                        Log.e("hahaha", "Xu ly: getRxApi_1() onNext");
                        String s = "";
                        for (String item : rxApi_1.api1Datas) {
                            s += item;
                        }
                        textViewResult.setText(s);
                        Person p;
                        List<Person> list = new ArrayList<>();
                        for (Post item : rxApi_1.api2Datas) {
                            p = new Person(item.getText(), item.getId());
                            list.add(p);
                        }
                        setUpAdapterPerson(list);
                        Log.e("hahaha", "Xu ly: " + rxApi_1.api1Datas.size() + " "
                                + rxApi_1.api2Datas.size() + " " +
                                rxApi_1.api3Datas.size());
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e("hahaha", "Xu ly: getRxApi_1() onError");
                    }

                    @Override
                    public void onComplete() {
                        Log.e("hahaha", "Xu ly: getRxApi_1() onComplete");
                    }
                });

    }

    //Goi song song API ket hop Save database dung RxAndroid
    private void getMutilRxApi_AndSaveDatabase_1() {
        Observable<List<String>> api1 = myPresenter.getRxApi_1()
                .map(posts -> myPresenter.handleResult(posts)).toList().toObservable();
        Observable<List<Post>> api2 = myPresenter.getRxApi_1();
        Observable<List<Post>> api3 = myPresenter.getRxApi_1();
        Observable<RxApi_1> apiAll = Observable.zip(api1, api2, api3, (api1Datas, api2Datas, api3Datas) -> new RxApi_1(api1Datas, api2Datas, api3Datas));
        apiAll.subscribe(new Observer<RxApi_1>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                compositeDisposable.add(d);
                textViewRx_result.setText("Calling multile API and Savedata");
                recyclerViewPerson.setAdapter(null);
                Log.e("hahaha", "Xu ly: getMutilRxApi_AndSaveDatabase_1() onSubscribe " + Thread.currentThread().getName());

            }

            @Override
            public void onNext(@NonNull RxApi_1 rxApi_1) {
                Log.e("hahaha", "Xu ly: getMutilRxApi_AndSaveDatabase_1() onNext()" + Thread.currentThread().getName()); //Do onSubscribe main
                saveDatabase_RxMutilRxApi(rxApi_1);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void saveDatabase_RxMutilRxApi(RxApi_1 rxApi_1) {
        myPresenter.saveDatabase_Rx_Single()
                //.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> {
                    Log.e("hahaha", "Do onSubscribe " + Thread.currentThread().getName()); //Do onSubscribe main
                })
                .subscribe(new SingleObserver<String>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull String s) {
                        Log.e("hahaha", "onSuccess " + Thread.currentThread().getName());//onSuccess main
                        String s1 = "";
                        for (String item : rxApi_1.api1Datas) {
                            s1 += item;
                        }
                        textViewResult.setText(s);
                        Person p;
                        List<Person> list = new ArrayList<>();
                        for (Post item : rxApi_1.api2Datas) {
                            p = new Person(item.getText(), item.getId());
                            list.add(p);
                        }
                        setUpAdapterPerson(list);
                        Log.e("hahaha", "Xu ly: " + rxApi_1.api1Datas.size() + " "
                                + rxApi_1.api2Datas.size() + " " +
                                rxApi_1.api3Datas.size());
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e("hahaha", "onError " + Thread.currentThread().getName());
                    }
                });
    }

    private void callMutilAPI_Get_ListPost() {
        myPresenter.callMutilAPI_Get_ListPost().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        compositeDisposable.add(d);
                        Log.e("hahaha", "callMutilAPI_Get_ListPost() onSubscribe" + Thread.currentThread().getName());
                    }

                    @Override
                    public void onNext(@NonNull String s) {
                        Log.e("hahaha", "callMutilAPI_Get_ListPost() onNext " + s + " " + Thread.currentThread().getName());
                        textViewRx_result.setText(s + "");
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        Log.e("hahaha", "callMutilAPI_Get_ListPost() onComplete() " + Thread.currentThread().getName());
                        textViewRx_result.setText(myPresenter.getRxText());
                    }
                });
    }

    private void callMutilAPISequenceNeedData_Get_ListPost() {
        myPresenter.callMutilAPISequenceNeedData_Get_ListPost().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        compositeDisposable.add(d);
                        Log.e("hahaha", "callMutilAPI_Get_ListPost() onSubscribe" + Thread.currentThread().getName());
                    }

                    @Override
                    public void onNext(@NonNull String s) {
                        Log.e("hahaha", "callMutilAPI_Get_ListPost() onNext " + s + " " + Thread.currentThread().getName());
                        textViewRx_result.setText(s + "");
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        Log.e("hahaha", "callMutilAPI_Get_ListPost() onComplete() " + Thread.currentThread().getName());
                        textViewRx_result.setText(myPresenter.getRxText());
                    }
                });
    }

    private void callMutilAPIParalle_Get_ListPost() {
        myPresenter.callMutilAPIParallel_Get_ListPost().subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        compositeDisposable.add(d);
                        Log.e("hahaha", "callMutilAPIParalle_Get_ListPost() onSubscribe" + Thread.currentThread().getName());
                    }

                    @Override
                    public void onNext(@NonNull String s) {
                        Log.e("hahaha", "callMutilAPIParalle_Get_ListPost() onNext " + s + " " + Thread.currentThread().getName());
                        //textViewRx_result.setText(s + "");
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        Log.e("hahaha", "callMutilAPIParalle_Get_ListPost() onComplete() " + Thread.currentThread().getName());
                        //textViewRx_result.setText(myPresenter.getRxText());
                    }
                });
    }

    private class RxApi_1 {
        private List<String> api1Datas;
        private List<Post> api2Datas;
        private List<Post> api3Datas;

        public RxApi_1(List<String> api1Datas, List<Post> api2Datas, List<Post> api3Datas) {
            this.api1Datas = api1Datas;
            this.api2Datas = api2Datas;
            this.api3Datas = api3Datas;
        }
    }
}