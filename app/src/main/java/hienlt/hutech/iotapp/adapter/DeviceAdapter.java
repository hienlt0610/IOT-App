package hienlt.hutech.iotapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import hienlt.hutech.iotapp.R;
import hienlt.hutech.iotapp.interfaces.IOnOffClick;
import hienlt.hutech.iotapp.model.Device;

/**
 * Created by hienl on 12/15/2016.
 */

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Device> devices;
    private IOnOffClick onOffClick;

    public DeviceAdapter(Context context, ArrayList<Device> devices,IOnOffClick onOffClick){
        this.context = context;
        this.devices = devices;
        this.onOffClick = onOffClick;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_device,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Device device = devices.get(position);
        holder.btnOnOff.setTag(position);
        holder.tvDeviceName.setText(device.getName());
        if(device.isTurnOn()){
            holder.btnOnOff.setBackgroundResource(R.drawable.turn_on);
        }else{
            holder.btnOnOff.setBackgroundResource(R.drawable.turn_off);
        }
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDeviceName;
        ImageButton btnOnOff;

        public ViewHolder(View itemView) {
            super(itemView);
            tvDeviceName = (TextView) itemView.findViewById(R.id.tvDeviceName);
            btnOnOff = (ImageButton) itemView.findViewById(R.id.btnOnOff);
            setViewClick();
        }

        private void setViewClick() {
            btnOnOff.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = (int) view.getTag();
                    Device device = devices.get(position);
                    device.setTurnOn(!device.isTurnOn());
                    notifyDataSetChanged();
                    onOffClick.onOffClick(btnOnOff,position,device.isTurnOn());
//                    String action;
//                    if(device.isTurnOn()){
//                        action = "Out"+(position+1)+"=0";
//                    }else{
//                        action = "Out"+(position+1)+"=1";
//                    }
//                    try {
//                        callBack.publish(action.getBytes("utf-8"), Config.TOPIC_SERVER,2);
//                        device.setTurnOn(!device.isTurnOn());
//                        notifyDataSetChanged();
//                    } catch (UnsupportedEncodingException e) {
//                        Toast.makeText(context, "Đã có lỗi xảy ra, không thể thực thi", Toast.LENGTH_SHORT).show();
//                        e.printStackTrace();
//                    }
                }
            });
        }
    }
}
