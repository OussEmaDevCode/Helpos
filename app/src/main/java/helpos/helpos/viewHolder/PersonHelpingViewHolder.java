package helpos.helpos.viewHolder;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import helpos.helpos.R;

public class PersonHelpingViewHolder extends RecyclerView.ViewHolder {
    public TextView title;
    public Button remove;
    public Button call;

    public PersonHelpingViewHolder(View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.list_item_title);
        call = itemView.findViewById(R.id.list_item_phone);
        remove = itemView.findViewById(R.id.list_item_remove);
    }
}
