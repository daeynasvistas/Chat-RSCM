package pt.IPG.messenger.recylcerchat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import pt.IPG.messenger.MainActivity;
import pt.IPG.messenger.R;

/**
 * Created by Dytstudio.
 */

public class ConversationRecyclerView extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    // The items to display in your RecyclerView
    private List<ChatData> items;
    private Context mContext;
    // --- Luis --- envio ficheiro
 //   private List<File> imagesFiles;


    private final int DATE = 0, YOU = 1, ME = 2, IMAGE_ME = 3, IMAGE_YOU = 4;

    // Provide a suitable constructor (depends on the kind of dataset)
    public ConversationRecyclerView(Context context, List<ChatData> items ){ //List<File> imagesFiles) {
        this.mContext = context;
        this.items = items;
     //   this.imagesFiles = imagesFiles;
    }





    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return this.items.size();
    }

    @Override
    public int getItemViewType(int position) {
        //More to come
        if (items.get(position).getType().equals("0")) {
            return DATE;
        } else if (items.get(position).getType().equals("1")) {
            return YOU;
        }else if (items.get(position).getType().equals("2")) {
            return ME;
        }
        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        switch (viewType) {
            //----  enviar img

             //-------------------
            case DATE:
                View v1 = inflater.inflate(R.layout.layout_holder_date, viewGroup, false);
                viewHolder = new HolderDate(v1);
                break;
            case YOU:
                View v2 = inflater.inflate(R.layout.layout_holder_you, viewGroup, false);
                viewHolder = new HolderYou(v2);
                break;
            default:
                View v = inflater.inflate(R.layout.layout_holder_me, viewGroup, false);
                viewHolder = new HolderMe(v);
                break;
        }
        return viewHolder;
    }
    public void addItem(List<ChatData> item) {
        items.addAll(item);
        notifyDataSetChanged();
    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        switch (viewHolder.getItemViewType()) {
          //  case IMAGE_ME:
          //      HolderMe vh3 = (HolderMe) viewHolder;
          //      configureViewHolder4(vh3, position);
          //      break;
          //  case IMAGE_YOU:
          //      HolderYou vh4 = (HolderYou) viewHolder;
          //      configureViewHolder5(vh4, position);
          //      break;
            case DATE:
                HolderDate vh1 = (HolderDate) viewHolder;
                configureViewHolder1(vh1, position);
                break;
            case YOU:
                HolderYou vh2 = (HolderYou) viewHolder;
                configureViewHolder2(vh2, position);
                break;
            default:
                HolderMe vh = (HolderMe) viewHolder;
                configureViewHolder3(vh, position);
                break;
        }
    }

/*
    private void configureViewHolder4(HolderMe vh1, int position) {
        vh1.getTime().setText(items.get(position).getTime());
        String substring = items.get(position).getText().substring(2);

        byte[] decodedString = Base64.decode(substring, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        vh1.getImageView().setImageBitmap(decodedByte);
      /*  Picasso.get()
                .load(decodedByte)
                .resize(350,350)
                .centerCrop()
                .into(vh1.getImageView());
//        vh1.getChatText().setText(items.get(position).getText());


    }
    private void configureViewHolder5(HolderYou vh1, int position) {
        vh1.getTime().setText(items.get(position).getTime());
        String substring = items.get(position).getText().substring(2);
        byte[] decodedString = Base64.decode(substring, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        vh1.getImageView().setImageBitmap(decodedByte);
        /*
        Picasso.get()
                .load(substring)
                .resize(350,350)
                .centerCrop()
                .into(vh1.getImageView());
//        vh1.getChatText().setText(items.get(position).getText());

    }
*/

    private void configureViewHolder3(HolderMe vh1, int position) {
            vh1.getTime().setText(items.get(position).getTime());
            if(items.get(position).getText().startsWith("5_")){
                String substring = items.get(position).getText().substring(2);
                byte[] decodedString = Base64.decode(substring, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                vh1.getImageView().setImageBitmap(decodedByte);

                /*
                Picasso.get()
                        .load(decodedByte)
                        .resize(350,350)
                        .centerCrop()
                        .into(vh1.getImageView());
*/
            }
            else {
                 vh1.getImageView().setVisibility(View.GONE);
                 vh1.getChatText().setText(items.get(position).getText());
                }


    }

    private void configureViewHolder2(HolderYou vh1, int position) {
            vh1.getTime().setText(items.get(position).getTime());

            if(items.get(position).getText().startsWith("5_")){
                String substring = items.get(position).getText().substring(2);
                byte[] decodedString = Base64.decode(substring, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                vh1.getImageView().setImageBitmap(decodedByte);

              /*
                Picasso.get()
                        .load(substring)
                        .resize(350,350)
                        .centerCrop()
                        .into(vh1.getImageView());
               */
            }
            else {
                vh1.getImageView().setVisibility(View.GONE);
                vh1.getChatText().setText(items.get(position).getText());
            }

    }
    private void configureViewHolder1(HolderDate vh1, int position) {
            vh1.getDate().setText(items.get(position).getText());
    }

}
