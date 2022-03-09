package com.raywenderlich.myapplication;

import android.util.Log;

import com.raywenderlich.myapplication.model.Person;
import com.raywenderlich.myapplication.model.Post;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
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

        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {

                if (!response.isSuccessful()) {
                    //textViewResult.setText("Code: " + response.code());
                    presenterInterface.onCallAPI_Get_ListPost(-1, response.code() + "");
                    return;
                }

                List<Post> posts = response.body();
                String result = "";
                for (Post post : posts) {
                    String content = "";
                    content += "ID: " + post.getId() + "\n";
                    content += "User ID: " + post.getUserId() + "\n";
                    content += "Title: " + post.getTitle() + "\n";
                    content += "Text: " + post.getText() + "\n\n";

                    result += content;
                }
                presenterInterface.onCallAPI_Get_ListPost(1, result);
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                presenterInterface.onCallAPI_Get_ListPost(-1, t.getMessage());
            }
        });
    }

    public Single<String> callAPI_Get_SaveDatabase_Rx_Single() {
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
