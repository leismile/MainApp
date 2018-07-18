package com.ubtechinc.alpha.provider.contracts;

import com.ubtechinc.alpha.provider.EntityManagerHelper;
import com.ubtechinc.alpha.upload.photo.PhotoInfo;

/**
 * @desc :
 * @author: Logic
 * @email : logic.peng@ubtech.com
 * @time : 2017/7/29
 * @modifier:
 * @modify_time:
 */

public final class PhotoInfoContract extends AlphaContract {

    private static final String TABLE_NAME = EntityManagerHelper.DB_PHOTO_INFO_TABLE;

    @Override
    public String getName() {
        return TABLE_NAME;
    }

    @Override
    public String getIdSqlString(String id) {
        return id;
    }

    @Override
    public Class getTableEntityClazz() {
        return PhotoInfo.class;
    }
}
