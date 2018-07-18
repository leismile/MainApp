package com.ubtechinc.alpha.service.sysevent.receiver;

import android.text.TextUtils;
import android.util.Log;

import com.ubtech.utilcode.utils.StringUtils;
import com.ubtechinc.alpha.service.ProcessLifeKeyguard;
import com.ubtechinc.alpha.service.sysevent.EventHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @author : kevin.liu@ubtrobot.com
 * @description :
 * @date : 2018/4/20
 * @modifier :
 * @modify time :
 */
public final class ReceiverWrapper implements ProcessLifeKeyguard.ProcessDiedObserver {
    private static final ReceiverWrapper mWrapper = new ReceiverWrapper();
    private HashMap<String, List<EventHandler>> mActionHandlerMap;

    private final ReentrantLock mLock = new ReentrantLock();
    public ReentrantLock getLock(){
        return this.mLock;
    }

    private ReceiverWrapper() {
        mActionHandlerMap = new HashMap<>();
        ProcessLifeKeyguard.subscribeProcessDied(this);
    }

    public static ReceiverWrapper get() {
        return mWrapper;
    }

    public List<EventHandler> findHandlers(String action) {
        mLock.lock();
        try {
            final List<EventHandler> list = get(action);
            return list == null ? null : Collections.unmodifiableList(list);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            mLock.unlock();
        }
        return null;
    }

    public List<EventHandler> findHighestHandlers(String action) {
        mLock.lock();
        final List<EventHandler> handlers = findHandlers(action);
        try {
            if(handlers == null){
                return null;
            }
            final int size = handlers.size();
            int index = 0;
            if (handlers != null && size > 0) {
                final EventHandler handlerMessage = handlers.get(0);
                final int highestPriority = handlerMessage.getPriority();
                if (size > 1) {
                    for (int i = 1; i < size; i++) {

                        if (handlers.get(i).getPriority() == highestPriority) {
                            index = i;
                            continue;
                        }else {
                            break;
                        }

                    }
                    return Collections.unmodifiableList(handlers.subList(0, index + 1));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            mLock.unlock();
        }
        return handlers;
    }

    public List<EventHandler> findPriorityHandlers(int priority, String action) {
        mLock.lock();
        final List<EventHandler> ret = new ArrayList<>();
        try {
            final List<EventHandler> handlers = findHandlers(action);
            if (handlers != null && handlers.size() > 0) {
                for (EventHandler h :
                        handlers) {
                    if (h.getPriority() == priority)
                        ret.add(h);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            mLock.unlock();
        }
        return Collections.unmodifiableList(ret);
    }

    /**
     * @param action
     * @return 某一种事件优先级最高的监听者
     */
    public final EventHandler findHighestHandler(String action) {
        return findHighestHandler(action, 0);
    }

    /**
     * @param action
     * @param from
     * @return 返回某一种事件优先级为from的监听者
     */
    public final EventHandler findHighestHandler(String action, int from) {
        mLock.lock();
        try {
            List<EventHandler> handles = get(action);
            if (handles != null && handles.size() > from) {
                return handles.get(from);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            mLock.unlock();
        }

        return null;
    }

    /**
     * 根据 action 和 uuid 查找事件处理器
     *
     * @param action
     * @param uuid
     * @return 事件处理器
     */
    public final EventHandler findHandler(final String action, final String uuid) {
        mLock.lock();
        try {
            List<EventHandler> handles = mActionHandlerMap.get(action);
            if (handles != null && handles.size() > 0) {
                for (EventHandler handle :
                        handles) {
                    if (StringUtils.equals(handle.getUuid(), uuid)) {
                        return handle;
                    }
                }
            }
        }catch (Exception e){
            Log.e("findHandler",""+e.getMessage());
        }finally {
            mLock.unlock();
        }
        return null;
    }




    public final int removeHandlerByUuid(String uuid){
        mLock.lock();
        try {
            if (TextUtils.isEmpty(uuid)) {
                return -2;
            }
            final Iterator<String> iterator = mActionHandlerMap.keySet().iterator();
            String action;
            List<EventHandler> handlers;
            int ret = -2;
            while (iterator.hasNext()) {
                action = iterator.next();
                handlers = mActionHandlerMap.get(action);

                if (handlers != null && handlers.size() > 0) {
                    for (int i = handlers.size()-1; i > -1; i--) {
                        if (StringUtils.isEquals(handlers.get(i).getUuid(), uuid)) {
                            Log.d("SysEventService","remove:"+handlers.get(i).toString());
                            handlers.remove(i);
                            ret = 0;
                        }
                    }

                }
            }
            return ret;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mLock.unlock();
        }
        return -2;
    }

    /**
     * 移除监听
     *
     * @param handler
     */
    public final  int removeHandler(final EventHandler handler) {
        mLock.lock();
        try {
            if (handler == null)
                return -2;

            String action = handler.getAction();
            String uuid = handler.getUuid();
            if (mActionHandlerMap.containsKey(action)) {
                List<EventHandler> handles = mActionHandlerMap.get(action);
                if (handles != null && handles.size() > 0) {
                    for (int i = handles.size()-1; i > -1; i--) {
                        if (StringUtils.isEquals(handles.get(i).getUuid(), uuid)) {
                            handles.remove(i);
                            Log.d("SysEventService","remove:uuid"+uuid+"action:"+action);
                            return 0;
                        }
                    }
                } else {
                    return -2; // not found!
                }
            } else {
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("removeHandler",""+e.getMessage());
        } finally {
            mLock.unlock();
        }

        return -2;
    }

    public final int removeHandlerBySkillName(String skillName) {
        mLock.lock();
        try {
            if (TextUtils.isEmpty(skillName)) {
                return -2;
            }
            final Iterator<String> iterator = mActionHandlerMap.keySet().iterator();
            String action;
            List<EventHandler> handlers;
            int ret = -2;
            while (iterator.hasNext()) {
                action = iterator.next();
                handlers = mActionHandlerMap.get(action);

                if (handlers != null && handlers.size() > 0) {
                    for (int i = handlers.size()-1; i > -1; i--) {
                        if (StringUtils.isEquals(handlers.get(i).getSkillName(), skillName)) {
                            Log.d("SysEventService","remove:"+handlers.get(i).toString());
                            handlers.remove(i);
                            ret = 0;
                        }
                    }
                }
            }
            return ret;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mLock.unlock();
        }
        return -2;
    }

    private List<EventHandler> get(String action){
        mLock.lock();
        try {
            return mActionHandlerMap.get(action);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            mLock.unlock();
        }
        return null;
    }

    /**
     * 增加监听
     *
     * @param handler
     */
    public final int addHandler(EventHandler handler) {
        mLock.lock();
        try {
            if(handler == null)
                return -1;
            String action = handler.getAction();
            List<EventHandler> handles = get(action);
            if (handles == null) {
                List<EventHandler> list = new ArrayList<>();
                addHandler(handler, list);
            } else if (handles.size() == 0) {
                addHandler(handler, handles);
            } else {
                addHandlerDirectly(handles, handler);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            mLock.unlock();
        }
        return 0;
    }

    private void addHandler(final EventHandler handler, List<EventHandler> list) {
        mLock.lock();
        addHandlerDirectly(list, handler);
        String action = handler.getAction();
        try {
            mActionHandlerMap.put(action, list);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mLock.unlock();
        }
    }

    private void addHandlerDirectly(List<EventHandler> list, EventHandler handler) {
        mLock.lock();
        try {
            //排序添加
            int priority = handler.getPriority();
            int index = -1;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getPriority() < priority) {
                    index = i;
                    break;
                }
            }
            if (index == -1) {
                list.add(handler);
            } else {
                list.add(index, handler);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mLock.unlock();
        }
    }

    @Override
    public void onProcessDied(int pid, int uid) {
        mLock.lock();
        try {
            final Iterator<String> iterator = mActionHandlerMap.keySet().iterator();
            String key;
            List<EventHandler> collections;
            EventHandler handlerMessage;
            while (iterator.hasNext()) {
                key = iterator.next();
                collections = mActionHandlerMap.get(key);
                if (collections.size() == 0) {
                    return;
                }
                for (int i = collections.size() - 1; i > -1; i--) {
                    if ((handlerMessage = collections.get(i)).getPid() == pid) {
                        removeHandler(handlerMessage);
                        return;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            mLock.unlock();
        }

    }
}
