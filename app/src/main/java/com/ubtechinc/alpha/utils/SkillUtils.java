package com.ubtechinc.alpha.utils;

import android.content.res.Resources;

import com.ubtechinc.alpha.app.AlphaApplication;
import com.ubtechinc.services.alphamini.R;
import com.ubtrobot.master.Master;
import com.ubtrobot.master.interactor.MasterInteractor;
import com.ubtrobot.master.skill.SkillsProxy;

/**
 * Created by lulin.wu on 2018/5/11.
 */

public class SkillUtils {
    private static String[] mSkillWhiteList;
    public static String[] loadSkillWhiteList(){
        Resources res = AlphaApplication.getContext().getResources();
        String[] skillWhiteList = res.getStringArray(R.array.skill_white_list);
        mSkillWhiteList = skillWhiteList;
        return skillWhiteList;
    }
    /**
     * 判断skill 是否在不触发活跃状态的skill列表中
     *
     * @return
     */
    public static boolean isInNotActiveSkills(String skillName){
        if(mSkillWhiteList == null){
            mSkillWhiteList = loadSkillWhiteList();
        }
        for(String name:mSkillWhiteList){
            if(name.equals(skillName)){
                return true;
            }
        }
        return false;
    }

    public static SkillsProxy getSkill(){
        MasterInteractor interactor = Master.get().getOrCreateInteractor("robot:" + AlphaApplication.getContext().getPackageName());
        SkillsProxy  aSkillsProxy = interactor.createSkillsProxy();
        return aSkillsProxy;
    }
}
