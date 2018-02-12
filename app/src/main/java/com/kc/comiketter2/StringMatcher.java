package com.kc.comiketter2;

import android.util.Log;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.Status;

/**
 * Created by HIDE on 2017/11/13.
 */

//サークル配置の正規表現マッチング
public class StringMatcher {
    //配置表示パターン。語尾abは無い場合が多い。後ろから探索
    final private static String EVENT_SPACE_PATTERN = ".*([a-zA-ZＡ-Ｚあ-んア-ン]).?([0-9０-９][0-9０-９])";
    final private static String AB = ".*(ab)"; //abを探索→無ければa|bで探索
    final private static String AOrB = ".*(a|b)";

    //ホールHashMap
    final private static Map<Integer, String> holeHashMap = new HashMap<Integer, String>(){
        {
            put(0, "");
            put(1, "東1");
            put(2, "東2");
            put(3, "東3");
            put(4, "東4");
            put(5, "東5");
            put(6, "東6");
            put(7, "東7");
            put(8, "東8");
//            put(101, "西1");
//            put(102, "西2");
//            put(103, "西3");
//            put(104, "西4");
        }
    };

    final private static String[] HOLE_PATTERN = new String[]{
            "", //ダミーデータ
            "A0[1-9]|A[1-2][0-9]|A3[0-6]|[B-L]", //東１
            "A3[7-9]|A4[0-9]|A5[0-3]|[M-Z]", //東２
            "A5[4-9]|A[6-8][0-9]|[ア-サ]", //東３
            "シ5[4-9]|シ[6-8][0-9]|[ム-ロ]", //東４
            "シ3[7-9]|シ4[0-9]|シ5[0-3]|[ネ-ミ]", //東５
            "シ0[1-9]|シ[1-2][0-9]|シ3[0-6]|[ス-ヌ]", //東６
            "[あ-の]", //東７
            "[ま-も]", //東８
//            "", //西１
//            "", //西２
//            "", //西３
//            "", //西４


    };

    //コミケ関係の日付記載パターン
    final private static String[] comikeEventPattern = new String[]{
            "[１-３1-3一二三]日目",
            "[月火水木金土]曜?",
            "日曜",
            "初日",
            "[東西]" + EVENT_SPACE_PATTERN,
            "[CＣ][0-9０-９]{2,3}.*日曜?"
//            ,"砲|ﾃｨｱ|ティア"
    };

    //どのパターンが何日目にマッチするのかをここで定義。
    final private static String firstDay = "([1１一]日目|金曜?日?|初日)" + "|(29|２９)日";
    final private static String secondDay = "([2２二]日目|土曜?日?)" + "|(30|３０)日";
    final private static String thirdDay = "([3３三]日目|日曜?日?)" + "|(31|３１)日";


    /**
     * コミケ専用のイベント名サーチメソッド
     * @param name 名前
     * @return イベント名
     */
    public static String getEventName(String name){
        //イベント名パターンを持っているか
        if (getSpace(name) != null){
            //イベント名でフィルタ、イベント名を抽出
            String eventName = getEventName(name, comikeEventPattern);
            if (eventName != null){
                return eventName;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * イベント名抽出メソッド
     * @param name 名前
     * @param eventNames サーチするイベント名称
     * @return イベント名
     */
    public static String getEventName(String name, String[] eventNames) {
        StringBuilder builder = new StringBuilder();
        //ORサーチ
        builder.append("(");
        for (int i = 0; i < eventNames.length ; i++){
            builder.append(eventNames[i] + "|");
            if (i == eventNames.length - 1){
                int index = builder.lastIndexOf("|");
                builder.deleteCharAt(index);
                builder.append(")");
            }
        }

        Pattern pattern = Pattern.compile(builder.toString());
        Matcher matcher = pattern.matcher(name);

        if (matcher.find()){
            String match = matcher.group(1);
            Log.d("Event", match);
            return match;
        } else {
            return null;
        }
    }

    /**
     * 配置を文字列の後ろから探し抽出する。
     * @param name
     * @return
     */
    public static String getSpace(String name){
        Pattern patternSpace = Pattern.compile(EVENT_SPACE_PATTERN);
        Matcher matcherSpace = patternSpace.matcher(name);
        if (matcherSpace.find()){
            Pattern patternAB = Pattern.compile(AB);
            Matcher matcherAB = patternAB.matcher(name);

            String[] match;
            if (matcherAB.find()){
                match = new String[]{
                        Normalizer.normalize(matcherSpace.group(1), Normalizer.Form.NFKC),
                        Normalizer.normalize(matcherSpace.group(2), Normalizer.Form.NFKC),
                        Normalizer.normalize(matcherAB.group(1), Normalizer.Form.NFKC)};

            } else {
                Pattern patternAOrB = Pattern.compile(AOrB);
                Matcher matcherAOrB = patternAOrB.matcher(name);

                if (matcherAOrB.find()){
                    match = new String[]{
                            Normalizer.normalize(matcherSpace.group(1), Normalizer.Form.NFKC),
                            Normalizer.normalize(matcherSpace.group(2), Normalizer.Form.NFKC),
                            Normalizer.normalize(matcherAOrB.group(1), Normalizer.Form.NFKC)};
                } else {
                    match = new String[]{
                            Normalizer.normalize(matcherSpace.group(1), Normalizer.Form.NFKC),
                            Normalizer.normalize(matcherSpace.group(2), Normalizer.Form.NFKC),
                            ""};
                }
            }

            Log.d("Space", match[0] + match[1] + match[2]);

            StringBuilder builder = new StringBuilder();
            builder.append(match[0]).append(match[1]).append(match[2]);
            return builder.toString();
        } else {
            return null;
        }
    }

    /**
     * 参加日を取得
     * 参加者かつ自動判別できない場合→「９」を返す
     * 非参加者→「９９」を返す
     * @param name
     * @return
     */
    public static Integer getParticipateDay(String name){
        //見つからなかったときは9（不明日）を返す。
        //非参加者のときは99（非参加）を返す
        if (getEventName(name) == null){
            return 99;
        } else {
            String[] dateArray = new String[]{"", firstDay, secondDay, thirdDay};
            for(Integer i_date = 1; i_date < dateArray.length; i_date++){
                Pattern pattern = Pattern.compile(dateArray[i_date]);
                Matcher matcher = pattern.matcher(name);
                if (matcher.find()){
                    return i_date;
                }
            }

            return 9;
        }
    }

    /**
     * サークル名を取得
     * @param status
     * @return
     */
    public static String getCircleName(Status status){
        Pattern pattern = Pattern.compile("貴サークル「(.*)」は");
        Matcher matcher = pattern.matcher(status.getText());
        if (matcher.find()){
            Log.d("CircleName","CircleName :" + matcher.group(1));
            return matcher.group(1);
        } else {
            Log.d("CircleName",  "status :" + status.getText());
            return null;
        }
    }

    public static Integer getHoleID(String circleSpace){
        for (Integer hole_i = 1; hole_i < HOLE_PATTERN.length; hole_i++){
            Pattern pattern = Pattern.compile(HOLE_PATTERN[hole_i]);
            Matcher matcher = pattern.matcher(circleSpace);
            if (matcher.find()){
                return hole_i;
            }
        }

        return null;
    }

    public static String getHoleName(Integer holeID){
        return holeHashMap.get(holeID); //キーが無ければnullが返る
    }

    public static Set<Integer> getMapKeys(){
        return holeHashMap.keySet();
    }
}