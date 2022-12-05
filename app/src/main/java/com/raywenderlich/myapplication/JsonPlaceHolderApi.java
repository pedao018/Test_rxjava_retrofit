package com.raywenderlich.myapplication;

import com.raywenderlich.myapplication.model.Post;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.Call;
import retrofit2.http.GET;

public interface JsonPlaceHolderApi {

    @GET("posts")
    Call<List<Post>> getPosts();

    @GET("posts1")
    Observable<List<Post>> getRx_Posts();
}