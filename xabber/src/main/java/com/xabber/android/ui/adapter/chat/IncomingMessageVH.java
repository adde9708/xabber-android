package com.xabber.android.ui.adapter.chat;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.StyleRes;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.xabber.android.R;
import com.xabber.android.data.SettingsManager;
import com.xabber.android.data.database.messagerealm.MessageItem;
import com.xabber.android.data.entity.AccountJid;
import com.xabber.android.data.entity.UserJid;
import com.xabber.android.data.extension.avatar.AvatarManager;
import com.xabber.android.data.extension.muc.MUCManager;
import com.xabber.android.data.log.LogManager;
import com.xabber.android.utils.Utils;

import org.jxmpp.jid.parts.Resourcepart;

public class IncomingMessageVH  extends FileMessageVH {

    public ImageView avatar;
    public ImageView avatarBackground;

    IncomingMessageVH(View itemView, MessageClickListener messageListener,
                      MessageLongClickListener longClickListener,
                      FileListener fileListener, @StyleRes int appearance) {
        super(itemView, messageListener, longClickListener, fileListener, appearance);
        avatar = itemView.findViewById(R.id.avatar);
        avatarBackground = itemView.findViewById(R.id.avatarBackground);
    }

    public void bind(MessageItem messageItem, boolean isMUC, boolean showOriginalOTR,
                     Context context, String userName, boolean unread, boolean checked,
                     ColorStateList colorStateList, boolean needTail) {
        super.bind(messageItem, isMUC, showOriginalOTR, context, unread, checked);

        // setup ARCHIVED icon
        statusIcon.setVisibility(messageItem.isReceivedFromMessageArchive() ? View.VISIBLE : View.GONE);

        // setup BACKGROUND
        Drawable balloonDrawable = context.getResources().getDrawable(needTail ? R.drawable.msg_in : R.drawable.msg);
        Drawable shadowDrawable = context.getResources().getDrawable(needTail ? R.drawable.msg_in_shadow : R.drawable.msg_shadow);
        shadowDrawable.setColorFilter(context.getResources().getColor(R.color.black), PorterDuff.Mode.MULTIPLY);
        messageBalloon.setBackgroundDrawable(balloonDrawable);
        messageShadow.setBackgroundDrawable(shadowDrawable);

        // setup BALLOON margins
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        layoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.avatar);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.tvFirstUnread);
        layoutParams.setMargins(
                Utils.dipToPx(needTail ? 2f : 11f, context),
                Utils.dipToPx(2f, context),
                Utils.dipToPx(needTail ? 2f : 10f, context),
                Utils.dipToPx(2f, context));
        messageShadow.setLayoutParams(layoutParams);

        // setup MESSAGE padding
        messageBalloon.setPadding(
                Utils.dipToPx(needTail ? 20f : 12f, context),
                Utils.dipToPx(8f, context),
                Utils.dipToPx(12f, context),
                Utils.dipToPx(8f, context));

        // setup BACKGROUND COLOR
        setUpMessageBalloonBackground(messageBalloon, colorStateList);

        setUpAvatar(messageItem, isMUC, userName, needTail);

        // hide empty message
        if (messageItem.getText().trim().isEmpty()) {
            messageBalloon.setVisibility(View.GONE);
            messageTime.setVisibility(View.GONE);
            avatar.setVisibility(View.GONE);
            avatarBackground.setVisibility(View.GONE);
            LogManager.w(this, "Empty message! Hidden, but need to correct");
        } else {
            messageBalloon.setVisibility(View.VISIBLE);
            messageTime.setVisibility(View.VISIBLE);
        }
    }

    private void setUpAvatar(MessageItem messageItem, boolean isMUC, String userName, boolean needTail) {
        boolean needAvatar = isMUC ? SettingsManager.chatsShowAvatarsMUC() : SettingsManager.chatsShowAvatars();

        if (!needAvatar) {
            avatar.setVisibility(View.GONE);
            avatarBackground.setVisibility(View.GONE);
            return;
        }

        if (!needTail) {
            avatar.setVisibility(View.INVISIBLE);
            avatarBackground.setVisibility(View.INVISIBLE);
            return;
        }


        final UserJid user = messageItem.getUser();
        final AccountJid account = messageItem.getAccount();
        final Resourcepart resource = messageItem.getResource();
        avatar.setVisibility(View.VISIBLE);
        avatarBackground.setVisibility(View.VISIBLE);

        if (!isMUC) avatar.setImageDrawable(AvatarManager.getInstance().getUserAvatar(user, userName));
        else {
            if ((MUCManager.getInstance()
                    .getNickname(account, user.getJid().asEntityBareJidIfPossible())
                    .equals(resource))) {
                avatar.setImageDrawable(AvatarManager.getInstance().getAccountAvatar(account));
            } else {
                if (resource.equals(Resourcepart.EMPTY)) {
                    avatar.setImageDrawable(AvatarManager.getInstance().getRoomAvatar(user));
                } else {

                    String nick = resource.toString();
                    UserJid userJid = null;

                    try {
                        userJid = UserJid.from(user.getJid().toString() + "/" + resource.toString());
                        avatar.setImageDrawable(AvatarManager.getInstance()
                                .getOccupantAvatar(userJid, nick));

                    } catch (UserJid.UserJidCreateException e) {
                        LogManager.exception(this, e);
                        avatar.setImageDrawable(AvatarManager.getInstance()
                                .generateDefaultAvatar(nick, nick));
                    }
                }
            }
        }
    }
}