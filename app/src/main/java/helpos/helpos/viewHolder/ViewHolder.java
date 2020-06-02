package helpos.helpos.viewHolder;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import helpos.helpos.R;

public class ViewHolder extends RecyclerView.ViewHolder {
    public LinearLayout root;
    public TextView title;

    public ViewHolder(View itemView) {
        super(itemView);
        root = itemView.findViewById(R.id.list_item_root);
        title = itemView.findViewById(R.id.list_item_title);
    }

}
