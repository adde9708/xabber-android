package com.xabber.android.ui.adapter.chat;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xabber.android.R;
import com.xabber.android.data.SettingsManager;
import com.xabber.android.data.database.messagerealm.MessageItem;
import com.xabber.android.data.entity.AccountJid;
import com.xabber.android.data.entity.UserJid;
import com.xabber.android.data.extension.muc.MUCManager;
import com.xabber.android.data.log.LogManager;
import com.xabber.android.data.message.AbstractChat;
import com.xabber.android.data.roster.RosterManager;
import com.xabber.android.ui.color.ColorManager;

import org.jxmpp.jid.parts.Resourcepart;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

public class MessagesAdapter extends RealmRecyclerViewAdapter<MessageItem, BasicMessageVH>
        implements MessageVH.MessageClickListener, MessageVH.MessageLongClickListener {

    private static final String LOG_TAG = MessagesAdapter.class.getSimpleName();

    public static final int VIEW_TYPE_INCOMING_MESSAGE = 2;
    public static final int VIEW_TYPE_OUTGOING_MESSAGE = 3;
    private static final int VIEW_TYPE_ACTION_MESSAGE = 4;

    private final Context context;
    private final MessageVH.MessageClickListener messageListener;
    private final FileMessageVH.FileListener fileListener;
    private final Listener listener;

    // message font style
    private final int appearanceStyle = SettingsManager.chatsAppearanceStyle();
    private int accountColorLevel;
    private ColorStateList colorStateList;
    private boolean isMUC;
    private Resourcepart mucNickname;
    private String userName;
    private AccountJid account;
    private UserJid user;
    private int prevItemCount;
    private int unreadCount = 0;
    private boolean isCheckMode;

    private List<String> itemsNeedOriginalText = new ArrayList<>();
    private List<String> checkedItemIds = new ArrayList<>();

    public interface Listener {
        void onMessageNumberChanged(int prevItemCount);
        void onMessagesUpdated();
        void onChangeCheckedItems(int checkedItems);
    }

    public MessagesAdapter(
            Context context, RealmResults<MessageItem> messageItems,
            AbstractChat chat, MessageVH.MessageClickListener messageListener,
            FileMessageVH.FileListener fileListener, Listener listener) {
        super(context, messageItems, true);

        this.context = context;
        this.messageListener = messageListener;
        this.fileListener = fileListener;
        this.listener = listener;

        account = chat.getAccount();
        user = chat.getUser();
        userName = RosterManager.getInstance().getName(account, user);
        prevItemCount = getItemCount();
        accountColorLevel = ColorManager.getAccountColorLevel(account);
        colorStateList = ColorManager.getInstance().getChatIncomingBalloonColorsStateList(account);

        isMUC = MUCManager.getInstance().hasRoom(account, user.getJid().asEntityBareJidIfPossible());
        if (isMUC) mucNickname = MUCManager.getInstance().getNickname(account, user.getJid().asEntityBareJidIfPossible());
    }

    @Override
    public int getItemCount() {
        if (realmResults.isValid() && realmResults.isLoaded())
            return realmResults.size();
        else return 0;
    }

    @Override
    public int getItemViewType(int position) {
        MessageItem messageItem = getMessageItem(position);
        if (messageItem == null) return 0;

        if (messageItem.getAction() != null)
            return VIEW_TYPE_ACTION_MESSAGE;

        if (messageItem.isIncoming()) {
            if (isMUC && messageItem.getResource().equals(mucNickname)) {
                return VIEW_TYPE_OUTGOING_MESSAGE;
            }
            return VIEW_TYPE_INCOMING_MESSAGE;
        } else return VIEW_TYPE_OUTGOING_MESSAGE;
    }

    @Override
    public BasicMessageVH onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_ACTION_MESSAGE:
                return new ActionMessageVH(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_action_message, parent, false));

            case VIEW_TYPE_INCOMING_MESSAGE:
                return new IncomingMessageVH(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_incoming, parent, false),
                        this, this, fileListener, appearanceStyle);

            case VIEW_TYPE_OUTGOING_MESSAGE:
                return new OutgoingMessageVH(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_outgoing, parent, false),
                        this, this, fileListener, appearanceStyle);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(final BasicMessageVH holder, int position) {

        final int viewType = getItemViewType(position);
        MessageItem messageItem = getMessageItem(position);

        if (messageItem == null) {
            LogManager.w(LOG_TAG, "onBindViewHolder Null message item. Position: " + position);
            return;
        }

        // setup message uniqueId
        if (holder instanceof MessageVH)
            ((MessageVH)holder).messageId = messageItem.getUniqueId();

        // setup message as unread
        boolean unread = position == getItemCount() - unreadCount;

        // setup message as checked
        boolean checked = checkedItemIds.contains(messageItem.getUniqueId());

        // need show original OTR message
        boolean showOriginalOTR = itemsNeedOriginalText.contains(messageItem.getUniqueId());

        // need tail
        boolean needTail = viewType != getItemViewType(position + 1);

        switch (viewType) {
            case VIEW_TYPE_ACTION_MESSAGE:
                ((ActionMessageVH)holder).bind(messageItem, context, account, isMUC);
                break;

            case VIEW_TYPE_INCOMING_MESSAGE:
                ((IncomingMessageVH)holder).bind(messageItem, isMUC, showOriginalOTR, context,
                        userName, unread, checked, colorStateList, needTail);

                break;
            case VIEW_TYPE_OUTGOING_MESSAGE:
                ((OutgoingMessageVH)holder).bind(messageItem, isMUC, showOriginalOTR, context,
                        unread, checked, needTail);
                break;
        }
    }

    @Override
    public void onChange() {
        notifyDataSetChanged();
        listener.onMessagesUpdated();

        int itemCount = getItemCount();
        if (prevItemCount != itemCount) {
            listener.onMessageNumberChanged(prevItemCount);
            prevItemCount = itemCount;
        }
    }

    @Nullable
    public MessageItem getMessageItem(int position) {
        if (position == RecyclerView.NO_POSITION) return null;

        if (position < realmResults.size())
            return realmResults.get(position);
        else return null;
    }

    @Override
    public void onMessageClick(View caller, int position) {
        if (isCheckMode) addOrRemoveCheckedItem(position);
        else messageListener.onMessageClick(caller, position);
    }

    @Override
    public void onLongMessageClick(int position) {
        addOrRemoveCheckedItem(position);
    }

    public int findMessagePosition(String uniqueId) {
        for (int i = 0; i < realmResults.size(); i++) {
            if (realmResults.get(i).getUniqueId().equals(uniqueId)) return i;
        }
        return RecyclerView.NO_POSITION;
    }

    public boolean setUnreadCount(int unreadCount) {
        if (this.unreadCount != unreadCount) {
            this.unreadCount = unreadCount;
            return true;
        } else return false;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void addOrRemoveItemNeedOriginalText(String messageId) {
        if (itemsNeedOriginalText.contains(messageId))
            itemsNeedOriginalText.remove(messageId);
        else itemsNeedOriginalText.add(messageId);
    }

    /** Checked items */

    private void addOrRemoveCheckedItem(int position) {
        String uniqueId = getItem(position).getUniqueId();

        if (checkedItemIds.contains(uniqueId))
            checkedItemIds.remove(uniqueId);
        else checkedItemIds.add(uniqueId);

        boolean isCheckModePrevious = isCheckMode;
        isCheckMode = checkedItemIds.size() > 0;

        if (isCheckMode != isCheckModePrevious)
            notifyDataSetChanged();
        else notifyItemChanged(position);

        listener.onChangeCheckedItems(checkedItemIds.size());
    }

    public List<String> getCheckedItemIds() {
        return checkedItemIds;
    }

    public int getCheckedItemsCount() {
        return checkedItemIds.size();
    }

    public void resetCheckedItems() {
        if (checkedItemIds.size() > 0) {
            checkedItemIds.clear();
            isCheckMode = false;
            notifyDataSetChanged();
            listener.onChangeCheckedItems(checkedItemIds.size());
        }
    }
}