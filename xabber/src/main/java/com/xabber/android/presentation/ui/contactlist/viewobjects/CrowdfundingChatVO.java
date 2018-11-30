package com.xabber.android.presentation.ui.contactlist.viewobjects;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.xabber.android.R;
import com.xabber.android.data.Application;
import com.xabber.android.data.database.realm.CrowdfundingMessage;
import com.xabber.android.ui.color.ColorManager;
import com.xabber.android.utils.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.viewholders.FlexibleViewHolder;

public class CrowdfundingChatVO extends AbstractFlexibleItem<CrowdfundingChatVO.ViewHolder> {

    private final String id;
    private final Date time;
    private final int unreadCount;
    private final String messageText;
    private int accountColorIndicator;
    private int accountColorIndicatorBack;

    public CrowdfundingChatVO(String messageText, Date time, int unreadCount,
                              int accountColorIndicator, int accountColorIndicatorBack) {
        this.id = UUID.randomUUID().toString();
        this.time = time;
        this.unreadCount = unreadCount;
        this.messageText = messageText;
        this.accountColorIndicator = accountColorIndicator;
        this.accountColorIndicatorBack = accountColorIndicatorBack;
    }

    public static CrowdfundingChatVO convert(CrowdfundingMessage message, int unreadCount) {
        int accountColorIndicator = ColorManager.getInstance().getAccountPainter().getDefaultMainColor();
        int accountColorIndicatorBack = ColorManager.getInstance().getAccountPainter().getDefaultIndicatorBackColor();

        String text;
        Locale currentLocale = Application.getInstance().getResources().getConfiguration().locale;
        if (currentLocale.getLanguage().equals("ru"))
            text = message.getMessageRu();
        else text = message.getMessageEn();

        return new CrowdfundingChatVO(text, null, unreadCount,
                accountColorIndicator, accountColorIndicatorBack);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof CrowdfundingChatVO) {
            CrowdfundingChatVO inItem = (CrowdfundingChatVO) o;
            return this.id.equals(inItem.id);
        }
        return false;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_crowdfunding_chat_in_contact_list;
    }

    @Override
    public ViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new ViewHolder(view, adapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter adapter, ViewHolder holder, int position, List<Object> payloads) {
        Context context = holder.itemView.getContext();

        holder.tvTime.setText(StringUtils
                .getSmartTimeTextForRoster(context, time));

        /** set up UNREAD COUNT */
        if (unreadCount > 0) {
            holder.tvUnreadCount.setText(String.valueOf(unreadCount));
            holder.tvUnreadCount.setVisibility(View.VISIBLE);
        } else holder.tvUnreadCount.setVisibility(View.INVISIBLE);

        /** set up MESSAGE TEXT */
        if (messageText.isEmpty())
            holder.tvMessageText.setText(R.string.xabber_chat_description);
        else holder.tvMessageText.setText(messageText);

        /** set up ACCOUNT COLOR indicator */
        holder.accountColorIndicator.setBackgroundColor(accountColorIndicator);
        holder.accountColorIndicatorBack.setBackgroundColor(accountColorIndicatorBack);
    }

    public class ViewHolder extends FlexibleViewHolder {

        final TextView tvContactName;
        final TextView tvTime;
        final TextView tvUnreadCount;
        final TextView tvMessageText;
        final View accountColorIndicator;
        final View accountColorIndicatorBack;

        public ViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);

            accountColorIndicator = view.findViewById(R.id.accountColorIndicator);
            accountColorIndicatorBack = view.findViewById(R.id.accountColorIndicatorBack);
            tvContactName = (TextView) view.findViewById(R.id.tvContactName);
            tvTime = (TextView) view.findViewById(R.id.tvTime);
            tvUnreadCount = (TextView) view.findViewById(R.id.tvUnreadCount);
            tvMessageText = (TextView) view.findViewById(R.id.tvMessageText);
        }
    }

}
