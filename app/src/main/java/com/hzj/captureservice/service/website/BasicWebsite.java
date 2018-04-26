package com.hzj.captureservice.service.website;

import android.text.TextUtils;

import java.io.File;

/**
 * <p>Basic website.</p>
 * Created by Yan Zhenjie on 2017/3/16.
 */
public abstract class BasicWebsite implements WebSite {

    /**
     * Default index page.
     */
    protected final String INDEX_HTML;

    /**
     * Basic Website.
     *
     * @param rootPath site root directory.
     */
    public BasicWebsite(String rootPath) {
        this.INDEX_HTML = TextUtils.isEmpty(rootPath) ? "index.html" : (rootPath + File.separator + "index.html");
    }

    /**
     * Remove the '/' at the beginning and end of the string.
     *
     * @param target target string.
     * @return rule result.
     */
    public static String trimSlash(String target) {
        while (target.startsWith(File.separator)) target = target.substring(1);
        while (target.endsWith(File.separator)) target = target.substring(0, target.length() - 1);
        return target;
    }

    /**
     * Generates a registration name based on the file path.
     *
     * @param filePath file path.
     * @return registration name.
     */
    public static String getHttpPath(String filePath) {
        if (!filePath.startsWith(File.separator))
            filePath = File.separator + filePath;
        return filePath;
    }

}
