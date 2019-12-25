package com.kc.comiketter2.util;

import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;

/**
 * Created by HIDE on 2017/11/23.
 */

public class TaskManager {
  private static TaskManager manager = null;
  private static SparseArray<AsyncTask> taskList = new SparseArray<>();

  public static TaskManager getInstance() {
    if (manager == null) {
      manager = new TaskManager();
    }
    return manager;
  }

  public int put(AsyncTask task) {
    Log.d("Comiketter2", String.valueOf(taskList.size()));
    taskList.put(taskList.size(), task);
    return taskList.size() - 1;
  }

  public AsyncTask get(Integer taskID) {
    return taskList.get(taskID, null);
  }

  public void clear() {
    taskList.clear();
  }

  public void remove(Integer taskID) {
    taskList.remove(taskID);
  }

  public boolean hasTaskID(Integer taskID) {
    AsyncTask task = taskList.get(taskID, null);
    if (task == null) {
      return false;
    } else {
      return true;
    }
  }

}
