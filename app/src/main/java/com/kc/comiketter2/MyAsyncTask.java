package com.kc.comiketter2;

import android.app.Activity;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

/**
 * Created by HIDE on 2017/11/13.
 */

public class MyAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    public WeakReference<Activity> weakActivity;
    public WeakReference<UserLoadDialogFragment> weakDialog;

    public interface IAsyncTaskCallback{
        void onComplete(Object result);
    }

    public MyAsyncTask (IAsyncTaskCallback activity){
        this.weakActivity = new WeakReference<>((Activity) activity);
    }

    @Override
    protected Result doInBackground(Params... params) {
        return null;
    }

    @Override
    protected void onPostExecute(Result result) {
        ((IAsyncTaskCallback)weakActivity.get()).onComplete(result);
    }
}