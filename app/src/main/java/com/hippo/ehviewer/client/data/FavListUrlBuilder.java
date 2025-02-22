/*
 * Copyright 2016 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hippo.ehviewer.client.data;

import static com.hippo.widget.ContentLayout.ContentHelper.GOTO_FIRST_PAGE;
import static com.hippo.widget.ContentLayout.ContentHelper.GOTO_LAST_PAGE;
import static com.hippo.widget.ContentLayout.ContentHelper.GOTO_NEXT_PAGE;
import static com.hippo.widget.ContentLayout.ContentHelper.GOTO_PREV_PAGE;
import static com.hippo.widget.ContentLayout.ContentHelper.TYPE_SOMEWHERE;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.hippo.ehviewer.client.EhUrl;
import com.hippo.ehviewer.widget.GalleryInfoContentHelper;
import com.hippo.network.UrlBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FavListUrlBuilder implements Parcelable {
    private static final Pattern PATTERN_SEEK_DATE = Pattern.compile("seek=(\\d+)-(\\d+)-(\\d+)");
    private static final Pattern  PATTERN_JUMP_NODE = Pattern.compile("jump=(\\d)[ymwd]");
    private static final String TAG = FavListUrlBuilder.class.getSimpleName();

    public static final int FAV_CAT_ALL = -1;
    public static final int FAV_CAT_LOCAL = -2;

    private int mIndex;
    private String mKeyword;
    private int mFavCat = FAV_CAT_ALL;

    public void setIndex(int index) {
        mIndex = index;
    }

    public void setKeyword(String keyword) {
        mKeyword = keyword;
    }

    public void setFavCat(int favCat) {
        mFavCat = favCat;
    }

    public String getKeyword() {
        return mKeyword;
    }

    public int getFavCat() {
        return mFavCat;
    }

    public static boolean isValidFavCat(int favCat) {
        return favCat >= 0 && favCat <= 9;
    }

    public boolean isLocalFavCat() {
        return mFavCat == FAV_CAT_LOCAL;
    }


    public String jumpHrefBuild(String urlOld,String appendParam){
        Matcher seekM = PATTERN_SEEK_DATE.matcher(urlOld);
        Matcher jumpM = PATTERN_JUMP_NODE.matcher(urlOld);

        String urlNew;
        if (seekM.find()){
            urlNew = urlOld.replace(Objects.requireNonNull(seekM.group(0)),appendParam);
        }else if (jumpM.find()){
            urlNew = urlOld.replace(Objects.requireNonNull(jumpM.group(0)),appendParam);
        }else{
            urlNew = urlOld+"&"+appendParam;
        }
        return urlNew;
    }

    public String build(int pageAction, GalleryInfoContentHelper helper){
        switch (pageAction){
            default:
            case GOTO_FIRST_PAGE:
                return helper.firstHref;
            case GOTO_PREV_PAGE:
                return helper.prevHref;
            case GOTO_NEXT_PAGE:
            case TYPE_SOMEWHERE:
                return helper.nextHref;
            case GOTO_LAST_PAGE:
                return helper.lastHref;
        }
    }

    public String build() {
        UrlBuilder ub = new UrlBuilder(EhUrl.getFavoritesUrl());
        if (isValidFavCat(mFavCat)) {
            ub.addQuery("favcat", Integer.toString(mFavCat));
        } else if (mFavCat == FAV_CAT_ALL) {
//            ub.addQuery("favcat", "all");
        }
        if (!TextUtils.isEmpty(mKeyword)) {
            try {
                ub.addQuery("f_search", URLEncoder.encode(mKeyword, "UTF-8"));
                // Name
                ub.addQuery("sn", "on");
                // Tags
                ub.addQuery("st", "on");
                // Note
                ub.addQuery("sf", "on");
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Can't URLEncoder.encode " + mKeyword);
            }
        }
        if (mIndex > 0) {
            ub.addQuery("page", Integer.toString(mIndex));
        }
        return ub.build();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mIndex);
        dest.writeString(this.mKeyword);
        dest.writeInt(this.mFavCat);
    }

    public FavListUrlBuilder() {
    }

    protected FavListUrlBuilder(Parcel in) {
        this.mIndex = in.readInt();
        this.mKeyword = in.readString();
        this.mFavCat = in.readInt();
    }

    public static final Creator<FavListUrlBuilder> CREATOR = new Creator<FavListUrlBuilder>() {

        @Override
        public FavListUrlBuilder createFromParcel(Parcel source) {
            return new FavListUrlBuilder(source);
        }

        @Override
        public FavListUrlBuilder[] newArray(int size) {
            return new FavListUrlBuilder[size];
        }
    };
}
