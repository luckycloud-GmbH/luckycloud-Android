package com.seafile.seadroid2.ui.dialog;

import android.util.Log;

import com.seafile.seadroid2.SeafException;
import com.seafile.seadroid2.data.DataManager;
import com.seafile.seadroid2.data.SeafDirent;
import com.seafile.seadroid2.ui.CopyMoveContext;
import com.seafile.seadroid2.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * AsyncTask for copying/moving files
 */
public class CopyMoveTask extends TaskDialog.Task {
    public static final String DEBUG_TAG = "CopyMoveTask";
    private DataManager dataManager;
    private CopyMoveContext ctx;

    public CopyMoveTask(CopyMoveContext ctx, DataManager dataManager) {
        this.ctx = ctx;
        this.dataManager = dataManager;
    }

    @Override
    protected void runTask() {

        if (ctx.batch) {
            String fileNames = "";
            List<SeafDirent> dirents = new ArrayList<>();
            if (ctx.direntsJson != null) {
                dirents = DataManager.parseDirents(ctx.direntsJson);
            }
            if (dirents == null) {
                dirents = new ArrayList<>();
            }
            for (SeafDirent dirent : dirents) {
                if (dirent.isSearchedFile) {
                    try {
                        String dstParentPath = dataManager.getCachedRepoByID(ctx.dstRepoId).name + ctx.dstDir;
                        String parentPath = dataManager.getCachedRepoByID(dirent.repoID).name + Utils.getParentPath(dirent.path);
                        if (dirent.isDir()) {
                            parentPath = dataManager.getCachedRepoByID(dirent.repoID).name + Utils.getParentPath(Utils.removeLastPathSeperator(dirent.path));
                        }
                        if (dirent.path.equals("/"))
                            continue;
                        dataManager.updateRefreshPaths(dstParentPath);
                        if (ctx.isCopy()) {
                            dataManager.copy(dirent.repoID, Utils.pathSplit(dirent.path, dirent.name), dirent.name, ctx.dstRepoId, ctx.dstDir);
                        } else if (ctx.isMove()) {
                            dataManager.updateRefreshPaths(parentPath);
                            dataManager.move(ctx.srcRepoId, Utils.pathSplit(dirent.path, dirent.name), dirent.name, ctx.dstRepoId, ctx.dstDir, false);
                        }
                    } catch (SeafException e) {
                        setTaskException(e);
                    }
                } else {
                    fileNames += ":" + dirent.name;
                }
            }

            if (fileNames.equals("")) return;

            fileNames = fileNames.substring(1, fileNames.length());

            try {
                if (ctx.isCopy()) {
                    dataManager.copy(ctx.srcRepoId, ctx.srcDir, fileNames, ctx.dstRepoId, ctx.dstDir);
                } else if (ctx.isMove()) {
                    dataManager.move(ctx.srcRepoId, ctx.srcDir, fileNames, ctx.dstRepoId, ctx.dstDir, true);
                }
            } catch (SeafException e) {
                setTaskException(e);
            }
            return;
        }

        try {
            if (ctx.isCopy()) {
                dataManager.copy(ctx.srcRepoId, ctx.srcDir, ctx.srcFn, ctx.dstRepoId, ctx.dstDir);
            } else if (ctx.isMove()) {
                dataManager.move(ctx.srcRepoId, ctx.srcDir, ctx.srcFn, ctx.dstRepoId, ctx.dstDir, false);
            }
        } catch (SeafException e) {
            setTaskException(e);
        }
    }

}
