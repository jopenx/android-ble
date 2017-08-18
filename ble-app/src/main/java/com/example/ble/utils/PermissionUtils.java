package com.example.ble.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限管理工具类
 * https://github.com/smartbetter/android-lite-utils
 *
 * 需要在 Activity 中添加
 * //@Override
 * public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
 *     PermissionUtils.getInstance().onRequestPermissionsResult(this, requestCode, permissions, grantResults);
 * }
 */
public final class PermissionUtils {
    /**
     * 单例模式(静态内部类的方式)
     */
    private PermissionUtils() {
    }
    public static PermissionUtils getInstance() {
        return SingletonHolder.instance;
    }
    private static class SingletonHolder {
        private static final PermissionUtils instance = new PermissionUtils();
    }

    public static final String TAG = "PermissionUtils";
    private static int mRequestCode = -1;
    private static OnPermissionListener mOnPermissionListener;

    public interface OnPermissionListener {
        /**
         * 授权成功
         */
        void onPermissionGranted();
        /**
         * 授权失败
         * @param deniedPermissions 授权失败的列表
         */
        void onPermissionDenied(String[] deniedPermissions);
    }

    public abstract class RationaleHandler {
        private Context context;
        private int requestCode;
        private String[] permissions;

        protected abstract void showRationale();

        void showRationale(Context context, int requestCode, String[] permissions) {
            this.context = context;
            this.requestCode = requestCode;
            this.permissions = permissions;
            showRationale();
        }

        @TargetApi(Build.VERSION_CODES.M)
        public void requestPermissionsAgain() {
            ((Activity) context).requestPermissions(permissions, requestCode);
        }
    }

    /**
     * 请求权限
     *
     * @param context
     * @param requestCode 请求标记码
     * @param permissions 权限字符串
     * @param listener
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissions(Context context, int requestCode
            , String[] permissions, OnPermissionListener listener) {
        requestPermissions(context, requestCode, permissions, listener, null);
    }

    /**
     * 请求权限
     *
     * @param context
     * @param requestCode 请求标记码
     * @param permissions 权限字符串
     * @param listener
     * @param handler
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissions(Context context, int requestCode
            , String[] permissions, OnPermissionListener listener, RationaleHandler handler) {
        if (context instanceof Activity) {
            mRequestCode = requestCode;
            mOnPermissionListener = listener;
            String[] deniedPermissions = getDeniedPermissions(context, permissions);
            if (deniedPermissions.length > 0) {
                boolean rationale = shouldShowRequestPermissionRationale(context, deniedPermissions);
                if (rationale && handler != null) {
                    handler.showRationale(context, requestCode, deniedPermissions);
                } else {
                    ((Activity) context).requestPermissions(deniedPermissions, requestCode);
                }
            } else {
                if (mOnPermissionListener != null) {
                    Log.i(TAG, "requestPermissions is success");
                    mOnPermissionListener.onPermissionGranted();
                }
            }
        } else {
            throw new RuntimeException("Context must be an Activity");
        }
    }

    /**
     * 请求权限回调结果，对应 Activity 中 onRequestPermissionsResult() 方法。
     * //@Override
     * public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
     *     PermissionUtils.getInstance().onRequestPermissionsResult(this, requestCode, permissions, grantResults);
     * }
     *
     * @param context
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void onRequestPermissionsResult(Activity context, int requestCode, String[] permissions, int[]
            grantResults) {
        if (mRequestCode != -1 && requestCode == mRequestCode) {
            if (mOnPermissionListener != null) {
                String[] deniedPermissions = getDeniedPermissions(context, permissions);
                if (deniedPermissions.length > 0) {
                    Log.i(TAG, "onRequestPermissionsResult part of value is fail");
                    mOnPermissionListener.onPermissionDenied(deniedPermissions);
                } else {
                    Log.i(TAG, "onRequestPermissionsResult all value is success");
                    mOnPermissionListener.onPermissionGranted();
                }
            }
        }
    }

    /**
     * 获取请求权限中需要授权的权限
     *
     * @param context
     * @param permissions
     * @return
     */
    private String[] getDeniedPermissions(final Context context, final String[] permissions) {
        List<String> deniedPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
                deniedPermissions.add(permission);
            }
        }
        return deniedPermissions.toArray(new String[deniedPermissions.size()]);
    }

    /**
     * 是否彻底拒绝了某项权限
     *
     * @param context
     * @param deniedPermissions
     * @return
     */
    public boolean hasAlwaysDeniedPermission(final Context context, final String... deniedPermissions) {
        for (String deniedPermission : deniedPermissions) {
            if (!shouldShowRequestPermissionRationale(context, deniedPermission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否有权限需要说明提示
     *
     * @param context
     * @param deniedPermissions
     * @return
     */
    private boolean shouldShowRequestPermissionRationale(final Context context, final String... deniedPermissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false;
        boolean rationale;
        for (String permission : deniedPermissions) {
            rationale = ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permission);
            if (rationale) return true;
        }
        return false;
    }
}