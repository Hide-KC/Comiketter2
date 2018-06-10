package com.kc.comiketter2;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.Status;

/**
 * Created by HIDE on 2017/11/13.
 */

//サークル配置の正規表現マッチング
public class StringMatcher_New {
    //配置表示パターン。語尾abは無い場合が多い。後ろから探索
    final private static String EVENT_SPACE_PATTERN = ".*([a-zA-ZＡ-Ｚあ-んア-ン]).?([0-9０-９][0-9０-９])";
    final private static String AB = ".*(ab)"; //abを探索→無ければa|bで探索
    final private static String AOrB = ".*(a|b)";
    final private static String FILTER_SWITCH = "filter_switch";

    //コミケ関係の日付記載パターン
    final private static String[] comikeEventPattern = new String[]{
            "[１-３1-3一二三]日目",
            "[金土]",
            "日曜",
            "初日",
            "[東西]" + EVENT_SPACE_PATTERN,
            "[CＣ][0-9０-９]{2,3}.*日"
    };

    //どのパターンが何日目にマッチするのかをここで定義。
    final private static String firstDay = "([1１一]日目|金曜?日?|初日)" + "|(10|１０)日";
    final private static String secondDay = "([2２二]日目|土曜?日?)" + "|(11|１１)日";
    final private static String thirdDay = "([3３三]日目|日曜?日?)" + "|(12|１２)日";


    //イベント名サーチメソッドフィルタ対応版
    public static String getEventName(String name, boolean checkHasSpace, Context context){
        return "";
    }

    public static String getComiketName(String name){
        return getEventName(name);
    }

    /**
     * コミケ専用のイベント名サーチメソッド
     * @param name 名前
     * @return イベント名
     */
    private static String getEventName(String name){
        return "";
    }

    /**
     * イベント名抽出メソッド
     * @param name 名前
     * @param eventNames サーチするイベント名称
     * @return イベント名
     */
    private static String getEventName(String name, String[] eventNames, boolean enableRegex) {
        return "";
    }

    /**
     * 配置を文字列の後ろから探し抽出する。
     * @param name
     * @return
     */
    public static String getSpace(String name){
        return "";
    }

    /**
     * 参加日を取得
     * 参加者かつ自動判別できない場合→「９」を返す
     * 非参加者→「９９」を返す
     * @param name
     * @return
     */
    public static Integer getParticipateDay(String name){
        return 0;
    }

    /**
     * サークル名を取得
     * @param status
     * @return
     */
    public static String getCircleName(Status status){
        return "";
    }
}