package com.kc.comiketter2.util;

import android.os.AsyncTask;

import androidx.appcompat.app.AppCompatActivity;

import com.kc.comiketter2.ui.dialog.UserLoadDialogFragment;

import java.lang.ref.WeakReference;

/**
 * Created by HIDE on 2017/11/13.
 */

abstract public class MyAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

  public WeakReference<AppCompatActivity> weakActivity;
  public WeakReference<UserLoadDialogFragment> weakDialog;
  public long myID = 0;
  public long targetID = 0;
  public String slug = "";

  public interface IAsyncTaskCallback {
    void onComplete(Object result);
  }

  public MyAsyncTask(IAsyncTaskCallback activity) {
    this.weakActivity = new WeakReference<>((AppCompatActivity) activity);
  }

  @Override
  protected Result doInBackground(Params... params) {
    return null;
  }

  @Override
  protected void onPostExecute(Result result) {
    ((IAsyncTaskCallback) weakActivity.get()).onComplete(result);
  }
}