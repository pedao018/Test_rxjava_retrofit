package com.raywenderlich.myapplication;

import android.util.Log;

import com.raywenderlich.myapplication.model.Person;
import com.raywenderlich.myapplication.model.Post;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class Presenter {

    private PresenterInterface presenterInterface;
    private String apiPostText = "", rxText = "";
    private List<Person> personList;

    public Presenter(PresenterInterface presenterInterface) {
        this.presenterInterface = presenterInterface;
    }

    public String getApiPostText() {
        return apiPostText;
    }

    public String getRxText() {
        return rxText;
    }

    public List<Person> getPersonList() {
        if (personList == null) {
            personList = new ArrayList<>();
        }
        return personList;
    }

    public void callAPI_Get_ListPost() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://jsonplaceholder.typicode.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        JsonPlaceHolderApi jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);

        Call<List<Post>> call = jsonPlaceHolderApi.getPosts();
        Log.e("hahaha", "Hoang tat: " + Thread.currentThread().getName());
        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {

                if (!response.isSuccessful()) {
                    //textViewResult.setText("Code: " + response.code());
                    presenterInterface.onCallAPI_Get_ListPost(-1, response.code() + "");
                    return;
                }
                presenterInterface.onCallAPI_Get_ListPost(1, handleResult(response.body()));
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                presenterInterface.onCallAPI_Get_ListPost(-1, t.getMessage());
            }
        });
    }

    public void callAPI_Search_ListPost() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://jsonplaceholder.typicode.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        JsonPlaceHolderApi jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);

        Call<List<Post>> call = jsonPlaceHolderApi.getPosts();
        Log.e("hahaha", "Goi: " + Thread.currentThread().getName());
        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {

                if (!response.isSuccessful()) {
                    //textViewResult.setText("Code: " + response.code());
                    //presenterInterface.onCallAPI_Get_ListPost(-1, response.code() + "");
                    return;
                }
                Log.e("hahaha", "Hoang tat: " + Thread.currentThread().getName());
                //presenterInterface.onCallAPI_Get_ListPost(1, handleResult(response.body()));
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                //presenterInterface.onCallAPI_Get_ListPost(-1, t.getMessage());
            }
        });
    }

    public void callAPI_Get_ListPost(ObservableEmitter<String> emitter, int type) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://jsonplaceholder.typicode.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        JsonPlaceHolderApi jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);
        Call<List<Post>> call = jsonPlaceHolderApi.getPosts();
        if (!emitter.isDisposed())
            emitter.onNext("" + type);
        try {
            Response<List<Post>> response = call.execute();
            if (!response.isSuccessful()) {
                return;
            }
            Thread.sleep(3000);
            Log.e("hahaha", "Hoang tat: " + type);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String callAPI_Get_ListPostSequenceNeedData(ObservableEmitter<String> emitter, int type, String input) {
        String result = "";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://jsonplaceholder.typicode.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        JsonPlaceHolderApi jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);
        Call<List<Post>> call = jsonPlaceHolderApi.getPosts();
        if (!emitter.isDisposed())
            emitter.onNext("" + input);
        try {
            Response<List<Post>> response = call.execute();
            if (!response.isSuccessful()) {
                return result;
            }
            Thread.sleep(3000);
            result = input + " " + type + "_";
            Log.e("hahaha", "Hoang tat: " + type);
        } catch (Exception e) {
            e.printStackTrace();
            result = e.getMessage();
            return result;
        }
        return result;
    }

    public void callAPIParallel_Get_ListPost(ObservableEmitter<String> emitter, int type) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://jsonplaceholder.typicode.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        JsonPlaceHolderApi jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);

        Call<List<Post>> call = jsonPlaceHolderApi.getPosts();

        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {

                if (!response.isSuccessful()) {
                    return;
                }
                Log.e("hahaha", "Hoang tat1: " + type + " " + Thread.currentThread().getName());
                emitter.onNext("Complete: " + type);
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public Observable<String> callMutilAPI_Get_ListPost() {
        return Observable.create(emitter -> {
            for (int i = 1; i <= 5; i++) {
                callAPI_Get_ListPost(emitter, i);
            }
            //Nó sẽ chạy tuần tự hết api rồi mới tới onComplete
            if (!emitter.isDisposed())
                emitter.onComplete();
        });
    }

    public Observable<String> callMutilAPISequenceNeedData_Get_ListPost() {
        return Observable.create(emitter -> {
            String result1 = callAPI_Get_ListPostSequenceNeedData(emitter, 1, "On progress");
            String result2 = callAPI_Get_ListPostSequenceNeedData(emitter, 2, result1);
            String result3 = callAPI_Get_ListPostSequenceNeedData(emitter, 3, result2);
            String result4 = callAPI_Get_ListPostSequenceNeedData(emitter, 4, result3);
            String result5 = callAPI_Get_ListPostSequenceNeedData(emitter, 5, result4);
            rxText = result5 + "Complete.!!";

            //Nó sẽ chạy tuần tự hết api rồi mới tới onComplete
            if (!emitter.isDisposed())
                emitter.onComplete();
        });
    }

    public Observable<String> callMutilAPIParallel_Get_ListPost() {
        return Observable.create(emitter -> {
            callAPIParallel_Get_ListPost(emitter, 1);
            callAPIParallel_Get_ListPost(emitter, 2);
            callAPIParallel_Get_ListPost(emitter, 3);
            callAPIParallel_Get_ListPost(emitter, 4);
            callAPIParallel_Get_ListPost(emitter, 5);

            //Nó sẽ chạy tuần tự hết api rồi mới tới onComplete
            if (!emitter.isDisposed()) {
                rxText = "Complete";
                emitter.onComplete();
            }

        });
    }

    public String handleResult(List<Post> posts) {
        String result = "";
        for (Post post : posts) {
            String content = "";
            content += "ID: " + post.getId() + "\n";
            content += "User ID: " + post.getUserId() + "\n";
            content += "Title: " + post.getTitle() + "\n";
            content += "Text: " + post.getText() + "\n\n";

            result += content;
        }
        return result;
    }

    public Single<String> saveDatabase_Rx_Single() {
        return Single.create(emitter -> {
            try {
                Log.e("hahaha", "Xu ly: " + Thread.currentThread().getName());//Xu ly: RxCachedThreadScheduler-1
                saveDatabase();
                if (!emitter.isDisposed())
                    emitter.onSuccess("Lay tu database -> Tra ve List");
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }

    public Observable<List<Post>> getRxApi_1() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://jsonplaceholder.typicode.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();

        JsonPlaceHolderApi jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);
        return jsonPlaceHolderApi.getRx_Posts();
    }

    public Completable initDataPersonList() {
        return Completable.create(emitter -> {
            getPersonDatabase();
            if (!emitter.isDisposed())
                emitter.onComplete();
        });
    }


    private void saveDatabase() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private List<Person> getPersonDatabase() {
        personList = new ArrayList<>();
        try {
            Log.e("hahaha", "Xu ly: getDatabase() 1");
            Thread.sleep(5000);
            for (int i = 0; i < 25; i++) {
                personList.add(new Person("name" + i, i));
            }
            Log.e("hahaha", "Xu ly: getDatabase() 2");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            return personList;
        }
    }
}
