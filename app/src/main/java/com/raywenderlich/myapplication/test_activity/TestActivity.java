package com.raywenderlich.myapplication.test_activity;

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
import android.widget.Toast;

import com.raywenderlich.myapplication.MainActivity;
import com.raywenderlich.myapplication.Presenter;
import com.raywenderlich.myapplication.PresenterInterface;
import com.raywenderlich.myapplication.R;
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
import retrofit2.HttpException;

public class TestActivity extends AppCompatActivity implements PresenterInterface {


    private TextView textViewResult, textViewRx_result;
    private Button callMutilRxApiBtn, callRxApiBtn;
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
        setContentView(R.layout.activity_test);

        textViewResult = findViewById(R.id.text_view_result);
        textViewRx_result = findViewById(R.id.rx_result_tv);
        callRxApiBtn = findViewById(R.id.call_rxapi);
        callMutilRxApiBtn = findViewById(R.id.call_mutilrxapi);
        callRxApiBtn.setOnClickListener(view -> {
            getRxApi_1();
        });
        callMutilRxApiBtn.setOnClickListener(view -> {
            getMutilRxApi_AndSaveDatabase_1();
        });

        myPresenter = new Presenter(this);

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
        //compositeDisposable.dispose();
        Log.e("hahaha", "Destroy Dispose");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.e("hahaha", "Backpress Dispose");
    }


    private void setUpAdapterPerson(List<Person> personList) {
        personAdapter = new PersonAdapter(personList, this);
        recyclerViewPerson.setAdapter(personAdapter);
    }


    //Goi song song API ket hop Save database dung RxAndroid
    //Khoi can goi subscribeOn vi retrofit no tu dong dua vao thread Okhttp gi do
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
                        Toast.makeText(getApplicationContext(), "Hahahaha", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e("hahaha", "onError " + Thread.currentThread().getName());
                    }
                });
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
                        ((HttpException) e).response().raw().headers("nel");//{"success_fraction":0,"report_to":"cf-nel","max_age":604800}
                        /*
                        2022-12-05 23:26:38.252 10749-10792/com.raywenderlich.myapplication I/okhttp.OkHttpClient: date: Mon, 05 Dec 2022 16:26:29 GMT
2022-12-05 23:26:38.252 10749-10792/com.raywenderlich.myapplication I/okhttp.OkHttpClient: content-type: application/json; charset=utf-8
2022-12-05 23:26:38.253 10749-10792/com.raywenderlich.myapplication I/okhttp.OkHttpClient: content-length: 2
2022-12-05 23:26:38.254 10749-10792/com.raywenderlich.myapplication I/okhttp.OkHttpClient: x-powered-by: Express
2022-12-05 23:26:38.254 10749-10792/com.raywenderlich.myapplication I/okhttp.OkHttpClient: x-ratelimit-limit: 1000
2022-12-05 23:26:38.254 10749-10792/com.raywenderlich.myapplication I/okhttp.OkHttpClient: x-ratelimit-remaining: 999
2022-12-05 23:26:38.255 10749-10792/com.raywenderlich.myapplication I/okhttp.OkHttpClient: x-ratelimit-reset: 1670256439
2022-12-05 23:26:38.255 10749-10792/com.raywenderlich.myapplication I/okhttp.OkHttpClient: vary: Origin, Accept-Encoding
2022-12-05 23:26:38.255 10749-10792/com.raywenderlich.myapplication I/okhttp.OkHttpClient: access-control-allow-credentials: true
2022-12-05 23:26:38.256 10749-10792/com.raywenderlich.myapplication I/okhttp.OkHttpClient: cache-control: max-age=43200
2022-12-05 23:26:38.256 10749-10792/com.raywenderlich.myapplication I/okhttp.OkHttpClient: pragma: no-cache
2022-12-05 23:26:38.256 10749-10792/com.raywenderlich.myapplication I/okhttp.OkHttpClient: expires: -1
2022-12-05 23:26:38.256 10749-10792/com.raywenderlich.myapplication I/okhttp.OkHttpClient: x-content-type-options: nosniff
2022-12-05 23:26:38.257 10749-10792/com.raywenderlich.myapplication I/okhttp.OkHttpClient: etag: W/"2-vyGp6PvFo4RvsFtPoIWeCReyIC8"
2022-12-05 23:26:38.257 10749-10792/com.raywenderlich.myapplication I/okhttp.OkHttpClient: via: 1.1 vegur
2022-12-05 23:26:38.257 10749-10792/com.raywenderlich.myapplication I/okhttp.OkHttpClient: cf-cache-status: HIT
2022-12-05 23:26:38.258 10749-10792/com.raywenderlich.myapplication I/okhttp.OkHttpClient: age: 1167
2022-12-05 23:26:38.258 10749-10792/com.raywenderlich.myapplication I/okhttp.OkHttpClient: report-to: {"endpoints":[{"url":"https:\/\/a.nel.cloudflare.com\/report\/v3?s=99EG3od2ypP%2FJxLF6yIniIGKJOzTnvYMWRrCMyHWoHXPZKRXA8glFPRiWWsaPqsEWdRgkf7vTfpSiBOsJRDtD1Tk7pM8VUtJfiMeVnTgK3Sw%2B6yQtfNlh0aMF8Dya7dY1RKN5UnLTb2qqR7alKYB"}],"group":"cf-nel","max_age":604800}
2022-12-05 23:26:38.258 10749-10792/com.raywenderlich.myapplication I/okhttp.OkHttpClient: nel: {"success_fraction":0,"report_to":"cf-nel","max_age":604800}
2022-12-05 23:26:38.259 10749-10792/com.raywenderlich.myapplication I/okhttp.OkHttpClient: server: cloudflare
2022-12-05 23:26:38.259 10749-10792/com.raywenderlich.myapplication I/okhttp.OkHttpClient: cf-ray: 774e24d099a987e7-SIN
2022-12-05 23:26:38.259 10749-10792/com.raywenderlich.myapplication I/okhttp.OkHttpClient: alt-svc: h3=":443"; ma=86400, h3-29=":443"; ma=86400
2022-12-05 23:26:38.263 10749-10792/com.raywenderlich.myapplication I/okhttp.OkHttpClient: {}
2022-12-05 23:26:38.264 10749-10792/com.raywenderlich.myapplication I/okhttp.OkHttpClient: <-- END HTTP (2-byte body)
                        */
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

    @Override
    public void onCallAPI_Get_ListPost(int type, String result) {

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