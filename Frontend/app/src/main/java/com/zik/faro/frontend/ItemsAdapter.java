package com.zik.faro.frontend;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zik.faro.data.ActionStatus;
import com.zik.faro.data.Item;
import com.zik.faro.frontend.data.ItemParentInfo;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;

import java.util.LinkedList;
import java.util.List;


public class ItemsAdapter extends ArrayAdapter {
    //TODO (Code Review) Implement sorted list instead of Linkedlist
    public List<ItemParentInfo> list = new LinkedList<>();

    private Context context;

    FaroUserContext faroUserContext = FaroUserContext.getInstance();

    private static EventFriendListHandler eventFriendListHandler = EventFriendListHandler.getInstance();

    public ItemsAdapter(Context context, int resource) {
        super(context, resource);
        this.context = context;
    }

    public int getCount() {
        return this.list.size();
    }


    public void insertAtBeginning(ItemParentInfo itemParentInfo){
        list.add(0, itemParentInfo);
    }

    public void insertAtEnd(ItemParentInfo itemParentInfo){
        list.add(itemParentInfo);
    }

    public void removeFromPosition(int position){
        list.remove(position);
    }

    public void insertAtPosition(ItemParentInfo itemParentInfo, int position){
        list.add(position, itemParentInfo);
    }

    @Override
    public Object getItem(int position) {
        return this.list.get(position);
    }

    static class ImgHolder{
        CheckBox ITEM_CHECKBOX;
        TextView ITEM_NAME;
        TextView ITEM_COUNT;
        TextView ITEM_UNIT;
        TextView ASSIGNEE_NAME;
        ImageButton DELETE_ITEM;
    }

    @Override
    public boolean isEnabled(int position)
    {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        ItemParentInfo itemParentInfo = (ItemParentInfo) getItem(position);
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ImgHolder holder = new ImgHolder();

        switch ((String)parent.getTag()){
            case "EditAssignment":
             /*   if (item.getId() != null){
                    row = inflater.inflate(R.layout.item_cant_edit_row_style, parent, false);
                }else{*/
                    row = inflater.inflate(R.layout.item_can_edit_row_style, parent, false);
                    holder.DELETE_ITEM = (ImageButton) row.findViewById(R.id.deleteItem);
                    holder.DELETE_ITEM.setImageResource(R.drawable.delete);
                    holder.DELETE_ITEM.setId(position);
                    holder.DELETE_ITEM.setFocusable(false);
                    holder.DELETE_ITEM.setFocusableInTouchMode(false);
                    holder.DELETE_ITEM.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ImageButton imageButton = (ImageButton) v;
                            int viewPosition = imageButton.getId();
                            ItemParentInfo removeItemParentInfo = (ItemParentInfo) getItem(viewPosition);
                            list.remove(removeItemParentInfo);
                            notifyDataSetChanged();
                        }
                    });
                //}
                holder.ITEM_NAME = (TextView) row.findViewById(R.id.itemName);
                holder.ITEM_COUNT = (TextView) row.findViewById(R.id.itemCount);
                holder.ITEM_UNIT = (TextView) row.findViewById(R.id.itemUnit);
                holder.ASSIGNEE_NAME = (TextView) row.findViewById(R.id.asigneeName);
                row.setTag(holder);

                holder.ITEM_NAME.setText(itemParentInfo.getItem().getName());
                break;
            case "AssignmentLandingPageFragment":
            case "MyAssignmentsFragment":
                row = inflater.inflate(R.layout.assignment_update_item_row_style, parent, false);
                holder.ITEM_CHECKBOX = (CheckBox) row.findViewById(R.id.itemCheckBox);
                holder.ITEM_COUNT = (TextView) row.findViewById(R.id.itemCount);
                holder.ITEM_UNIT = (TextView) row.findViewById(R.id.itemUnit);
                holder.ASSIGNEE_NAME = (TextView) row.findViewById(R.id.asigneeName);
                row.setTag(holder);

                String myUserId = faroUserContext.getEmail();
                if(myUserId.equals(itemParentInfo.getItem().getAssigneeId())){
                    holder.ITEM_CHECKBOX.setText(itemParentInfo.getItem().getName());
                    holder.ITEM_CHECKBOX.setId(position);
                    if (itemParentInfo.getItem().getStatus() == ActionStatus.COMPLETE) {
                        holder.ITEM_CHECKBOX.setChecked(true);
                    }else{
                        holder.ITEM_CHECKBOX.setChecked(false);
                    }
                    holder.ITEM_CHECKBOX.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CheckBox clickedCheckBox = (CheckBox)v;
                            int viewPosition = clickedCheckBox.getId();
                            if (clickedCheckBox.isChecked()){
                                ItemParentInfo clickedItemParentInfo = (ItemParentInfo) getItem(viewPosition);
                                clickedItemParentInfo.getItem().setStatus(ActionStatus.COMPLETE);
                                list.remove(clickedItemParentInfo);
                                insertAtEnd(clickedItemParentInfo);
                                notifyDataSetChanged();
                            }else{
                                ItemParentInfo clickedItemParentInfo = (ItemParentInfo) getItem(viewPosition);
                                clickedItemParentInfo.getItem().setStatus(ActionStatus.INCOMPLETE);
                                list.remove(clickedItemParentInfo);
                                insertAtBeginning(clickedItemParentInfo);
                                notifyDataSetChanged();
                            }
                        }
                    });
                }else{
                    holder.ITEM_CHECKBOX.setText(itemParentInfo.getItem().getName());
                    holder.ITEM_CHECKBOX.setId(position);
                    holder.ITEM_CHECKBOX.setClickable(false);
                    if (itemParentInfo.getItem().getStatus() == ActionStatus.COMPLETE) {
                        holder.ITEM_CHECKBOX.setChecked(true);
                    }else{
                        holder.ITEM_CHECKBOX.setChecked(false);
                    }
                }
                break;
        }

        holder.ITEM_COUNT.setText(String.valueOf(itemParentInfo.getItem().getCount()));
        holder.ITEM_UNIT.setText(itemParentInfo.getItem().getUnit().toString());
        String fullName = eventFriendListHandler.getFriendFullNameFromID(itemParentInfo.getItem().getAssigneeId());
        holder.ASSIGNEE_NAME.setText(fullName);

        return row;
    }
}
