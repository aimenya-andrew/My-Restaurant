package com.paidapp.laughtercafe.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.paidapp.laughtercafe.Common.Common;
import com.paidapp.laughtercafe.Database.CartItem;
import com.paidapp.laughtercafe.EventBus.UpdateItemInCart;
import com.paidapp.laughtercafe.Model.AddonModel;
import com.paidapp.laughtercafe.Model.SizeModel;
import com.paidapp.laughtercafe.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyCartAdapter extends RecyclerView.Adapter<MyCartAdapter.MyViewHolder> {
    Context context;
    List<CartItem> cartItemsList;
    Gson gson;

    public MyCartAdapter(Context context, List<CartItem> cartItemsList) {
        this.context = context;
        this.cartItemsList = cartItemsList;
        this.gson = new Gson();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_cart_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(cartItemsList.get(position).getFoodImage())
                .into(holder.img_cart);
        holder.txt_food_name.setText(new StringBuilder(cartItemsList.get(position).getFoodName()));
        holder.txt_food_price.setText(new StringBuilder("")
        .append(cartItemsList.get(position).getFoodPrice() + cartItemsList.get(position).getFoodExtraPrice()));

        if (cartItemsList.get(position).getFoodSize() != null)
        {
            if (cartItemsList.get(position).getFoodSize().equals("Default"))
                holder.txt_food_size.setText(new StringBuilder("Size: ").append("Default"));
            else
            {
                SizeModel sizeModel = gson.fromJson(cartItemsList.get(position).getFoodSize(),new TypeToken<SizeModel>(){}.getType());
                holder.txt_food_size.setText(new StringBuilder("Size: ").append(sizeModel.getName()));
            }
        }

        if (cartItemsList.get(position).getFoodAddon() != null)
        {
            if (cartItemsList.get(position).getFoodAddon().equals("Default"))
                holder.txt_food_addon.setText(new StringBuilder("Addon: ").append("Default"));
            else
            {
                List<AddonModel> addonModels = gson.fromJson(cartItemsList.get(position).getFoodAddon(),
                        new TypeToken<List<AddonModel>>(){}.getType());
                holder.txt_food_addon.setText(new StringBuilder("Addon: ").append(Common.getListAddon(addonModels)));
            }
        }

        holder.numberButton.setNumber(String.valueOf(cartItemsList.get(position).getFoodQuantity()));

        holder.numberButton.setOnValueChangeListener((view, oldValue, newValue) -> {
            cartItemsList.get(position).setFoodQuantity(newValue);
            EventBus.getDefault().postSticky(new UpdateItemInCart(cartItemsList.get(position)));
        });
    }

    @Override
    public int getItemCount() {
        return cartItemsList.size();
    }

    public CartItem getItemAtPosition(int pos) {
        return cartItemsList.get(pos);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private Unbinder unbinder;
        @BindView(R.id.img_cart)
        ImageView img_cart;
        @BindView(R.id.txt_food_price)
        TextView txt_food_price;
        @BindView(R.id.txt_food_size)
        TextView txt_food_size;
        @BindView(R.id.txt_food_addon)
        TextView txt_food_addon;
        @BindView(R.id.txt_food_name)
        TextView txt_food_name;
        @BindView(R.id.number_button)
        ElegantNumberButton numberButton;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
        }
    }
}
