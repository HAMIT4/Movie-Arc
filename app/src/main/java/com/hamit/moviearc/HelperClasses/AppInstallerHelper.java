package com.hamit.moviearc.HelperClasses;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

public class AppInstallerHelper {
    private static final int INSTALL_PERMISSION_REQUEST_CODE = 1001;
    private static final String TAG = "AppInstallerHelper";

    private final Activity activity;
    private final String packageName;
    private final String downloadUrl;
    private final String appName;

    private ProgressDialog progressDialog;
    private InstallCallback callback;
    private long downloadId;

    public interface InstallCallback {
        void onInstallSuccess();
        void onInstallFailed(String error);
        void onAppOpened();
    }

    public AppInstallerHelper(Activity activity, String packageName, String downloadUrl, String appName) {
        this.activity = activity;
        this.packageName = packageName;
        this.downloadUrl = downloadUrl;
        this.appName = appName;
    }

    public void setCallback(InstallCallback callback) {
        this.callback = callback;
    }

    public void openOrInstallApp() {
        Log.d(TAG, "openOrInstallApp: Attempting to open " + appName);

        // First try to open the app using video intent (this tests if it's installed and working)
        if (tryOpenWithVideoIntent()) {
            Log.d(TAG, "openOrInstallApp: App opened successfully via video intent");
            if (callback != null) {
                callback.onAppOpened();
            }
        } else {
            Log.d(TAG, "openOrInstallApp: App not found or cannot be opened, starting download");
            downloadAndInstallApp();
        }
    }

    private boolean tryOpenWithVideoIntent() {
        Log.d(TAG, "Testing if " + appName + " can be opened by actually launching a short video");

        String testVideoUrl = "https://samplelib.com/lib/preview/mp4/sample-5s.mp4";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(testVideoUrl), "video/*");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage(packageName); // Direct to Drama Player if it exists

        try {
            activity.startActivity(intent);
            Log.d(TAG, "tryOpenWithVideoIntent: SUCCESS - Drama Player launched");

            // Schedule returning to this app after 3 seconds (shorter and more reliable)
            new android.os.Handler(activity.getMainLooper()).postDelayed(() -> {
                Intent returnIntent = new Intent(activity, activity.getClass());
                returnIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                try {
                    activity.startActivity(returnIntent);
                    Log.d(TAG, "Returned to app after 3 seconds");
                } catch (Exception e) {
                    Log.e(TAG, "Could not return to app: " + e.getMessage());
                }
            }, 3000);

            return true;
        } catch (ActivityNotFoundException e) {
            Log.d(TAG, "tryOpenWithVideoIntent: FAILED - Drama Player not found or cannot open URLs");
            return false;
        } catch (Exception e) {
            Log.e(TAG, "tryOpenWithVideoIntent: ERROR - " + e.getMessage());
            return false;
        }
    }


    private boolean isAppInstalled() {
        try {
            activity.getPackageManager().getPackageInfo(packageName, 0);
            Log.d(TAG, "isAppInstalled: Package found: " + packageName);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "isAppInstalled: Package not found: " + packageName);
            return false;
        }
    }

    private void openApp() {
        // This method is kept for compatibility but tryOpenWithVideoIntent is now the primary method
        tryOpenWithVideoIntent();
    }

    private void downloadAndInstallApp() {
        // Check for install permission (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!activity.getPackageManager().canRequestPackageInstalls()) {
                Log.d(TAG, "downloadAndInstallApp: Requesting install permission");
                requestInstallPermission();
                return;
            }
        }

        Log.d(TAG, "downloadAndInstallApp: Showing download confirmation");
        showDownloadConfirmationDialog();
    }

    private void showDownloadConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Install " + appName);
        builder.setMessage(appName + " is required to watch movies. Do you want to download and install it now?");

        builder.setPositiveButton("Download", (dialog, which) -> {
            Log.d(TAG, "User confirmed download");
            startDownload();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            Log.d(TAG, "User cancelled download");
            dialog.dismiss();
            if (callback != null) {
                callback.onInstallFailed("User cancelled download");
            }
        });

        builder.setCancelable(false);
        builder.show();
    }

    private void startDownload() {
        Log.d(TAG, "startDownload: Starting download process");

        try {
            // Show progress dialog
            progressDialog = new ProgressDialog(activity);
            progressDialog.setTitle("Downloading " + appName);
            progressDialog.setMessage("Please wait...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.setMax(100);
            progressDialog.show();

            // Use DownloadManager for reliable download
            DownloadManager downloadManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);

            if (downloadManager == null) {
                Toast.makeText(activity, "Download service not available", Toast.LENGTH_SHORT).show();
                if (callback != null) {
                    callback.onInstallFailed("Download service not available");
                }
                return;
            }

            // Create download request
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));

            // Set download details
            request.setTitle(appName);
            request.setDescription("Downloading " + appName + " app");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setAllowedOverMetered(true);
            request.setAllowedOverRoaming(true);

            // Set destination
            String fileName = appName.replace(" ", "") + "-" + System.currentTimeMillis() + ".apk";
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

            // Enqueue download
            downloadId = downloadManager.enqueue(request);
            Log.d(TAG, "startDownload: Download enqueued with ID: " + downloadId);

            // Monitor download progress
            monitorDownloadProgress(downloadId, downloadManager);

        } catch (Exception e) {
            Log.e(TAG, "startDownload: Error starting download", e);
            Toast.makeText(activity, "Error starting download", Toast.LENGTH_SHORT).show();
            if (callback != null) {
                callback.onInstallFailed("Error starting download: " + e.getMessage());
            }
        }
    }

    private void monitorDownloadProgress(long downloadId, DownloadManager downloadManager) {
        Log.d(TAG, "monitorDownloadProgress: Starting progress monitoring");

        new Thread(() -> {
            boolean downloading = true;

            while (downloading) {
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);

                try (Cursor cursor = downloadManager.query(query)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        @SuppressLint("Range") int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                        Log.d(TAG, "monitorDownloadProgress: Download status: " + status);

                        switch (status) {
                            case DownloadManager.STATUS_SUCCESSFUL:
                                Log.d(TAG, "monitorDownloadProgress: Download successful");
                                downloading = false;
                                activity.runOnUiThread(() -> {
                                    if (progressDialog != null && progressDialog.isShowing()) {
                                        progressDialog.dismiss();
                                    }
                                    installDownloadedApk(downloadId);
                                });
                                break;

                            case DownloadManager.STATUS_FAILED:
                                @SuppressLint("Range") int reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));
                                Log.e(TAG, "monitorDownloadProgress: Download failed, reason: " + reason);
                                downloading = false;
                                activity.runOnUiThread(() -> {
                                    if (progressDialog != null && progressDialog.isShowing()) {
                                        progressDialog.dismiss();
                                    }
                                    Toast.makeText(activity, "Download failed: " + getDownloadErrorString(reason), Toast.LENGTH_SHORT).show();
                                    if (callback != null) {
                                        callback.onInstallFailed("Download failed: " + getDownloadErrorString(reason));
                                    }
                                });
                                break;

                            case DownloadManager.STATUS_RUNNING:
                                // Update progress
                                @SuppressLint("Range") long bytesDownloaded = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                                @SuppressLint("Range") long bytesTotal = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                                if (bytesTotal > 0) {
                                    int progress = (int) ((bytesDownloaded * 100L) / bytesTotal);
                                    Log.d(TAG, "monitorDownloadProgress: Progress: " + progress + "%");
                                    activity.runOnUiThread(() -> {
                                        if (progressDialog != null && progressDialog.isShowing()) {
                                            progressDialog.setProgress(progress);
                                        }
                                    });
                                }
                                break;
                        }
                    } else {
                        Log.d(TAG, "monitorDownloadProgress: No download found with ID: " + downloadId);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "monitorDownloadProgress: Error querying download", e);
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "monitorDownloadProgress: Thread interrupted", e);
                    break;
                }
            }
        }).start();
    }

    private String getDownloadErrorString(int reason) {
        switch (reason) {
            case DownloadManager.ERROR_CANNOT_RESUME:
                return "Cannot resume download";
            case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                return "Device not found";
            case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                return "File already exists";
            case DownloadManager.ERROR_FILE_ERROR:
                return "File error";
            case DownloadManager.ERROR_HTTP_DATA_ERROR:
                return "HTTP data error";
            case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                return "Insufficient space";
            case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                return "Too many redirects";
            case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                return "Unhandled HTTP code";
            case DownloadManager.ERROR_UNKNOWN:
            default:
                return "Unknown error";
        }
    }

    private void installDownloadedApk(long downloadId) {
        Log.d(TAG, "installDownloadedApk: Starting installation");

        try {
            DownloadManager downloadManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri apkUri = downloadManager.getUriForDownloadedFile(downloadId);

            if (apkUri != null) {
                Log.d(TAG, "installDownloadedApk: APK URI found: " + apkUri.toString());

                Intent installIntent = new Intent(Intent.ACTION_VIEW);
                installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                try {
                    activity.startActivity(installIntent);
                    Log.d(TAG, "installDownloadedApk: Installation intent started");
                    if (callback != null) {
                        callback.onInstallSuccess();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "installDownloadedApk: Error starting installation", e);
                    Toast.makeText(activity, "Error installing app. Please enable 'Install from unknown sources'.", Toast.LENGTH_LONG).show();
                    if (callback != null) {
                        callback.onInstallFailed("Installation failed: " + e.getMessage());
                    }
                }
            } else {
                Log.e(TAG, "installDownloadedApk: Downloaded file not found");
                Toast.makeText(activity, "Downloaded file not found", Toast.LENGTH_SHORT).show();
                if (callback != null) {
                    callback.onInstallFailed("Downloaded file not found");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "installDownloadedApk: Unexpected error", e);
            Toast.makeText(activity, "Installation error", Toast.LENGTH_SHORT).show();
            if (callback != null) {
                callback.onInstallFailed("Unexpected error: " + e.getMessage());
            }
        }
    }

    private void requestInstallPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Install Permission Required");
            builder.setMessage("This app needs permission to install other apps. Please grant the permission to continue.");

            builder.setPositiveButton("Grant Permission", (dialog, which) -> {
                Log.d(TAG, "requestInstallPermission: User granted permission, opening settings");
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                activity.startActivityForResult(intent, INSTALL_PERMISSION_REQUEST_CODE);
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> {
                Log.d(TAG, "requestInstallPermission: User cancelled permission");
                dialog.dismiss();
                if (callback != null) {
                    callback.onInstallFailed("Install permission denied");
                }
            });

            builder.setCancelable(false);
            builder.show();
        }
    }

    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "handleActivityResult: Request code: " + requestCode);
        if (requestCode == INSTALL_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (activity.getPackageManager().canRequestPackageInstalls()) {
                    Log.d(TAG, "handleActivityResult: Install permission granted, restarting download");
                    showDownloadConfirmationDialog();
                    return true;
                } else {
                    Log.d(TAG, "handleActivityResult: Install permission denied");
                    Toast.makeText(activity, "Permission denied. Cannot install " + appName + ".", Toast.LENGTH_SHORT).show();
                    if (callback != null) {
                        callback.onInstallFailed("Install permission denied");
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public void cleanup() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}