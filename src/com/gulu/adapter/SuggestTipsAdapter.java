package com.gulu.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.amap.api.services.help.Tip;
import com.gulu.R;

public class SuggestTipsAdapter extends BaseAdapter implements Filterable {
	
	private Context mCxt;
	private final Object mLock = new Object();
	private List<Tip> tips;
	
	public SuggestTipsAdapter(Context mCxt, List<Tip> tips) {
		this.mCxt = mCxt;
		this.tips = tips;
	}
	
	@Override
	public int getCount() {
		if (tips == null || tips.isEmpty())
			return 0;
		
		return tips.size();
	}
	
	@Override
	public Object getItem(int position) {
		if (tips == null || tips.isEmpty())
			return null;
		
		return tips.get(position);
	}
	
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View mTipPane;
		TipViewHolder holder;
		if (convertView == null) {
			mTipPane = LayoutInflater.from(mCxt).inflate(R.layout.drop_down_suggest_tip,
					null);
			holder = new TipViewHolder();
			holder.mTipView = (TextView) mTipPane.findViewById(R.id.tip);
			mTipPane.setTag(holder);
		} else {
			mTipPane = convertView;
			holder = (TipViewHolder) mTipPane.getTag();
		}
		
		Tip tip = tips.get(position);
		
		StringBuilder sb = new StringBuilder(tip.getName());
		if (!TextUtils.isEmpty(tip.getDistrict())) {
			sb.append("(").append(tip.getDistrict()).append(")");
		}
		
		holder.mTipView.setText(sb.toString());
		
		return mTipPane;
	}
	
	@Override
	public Filter getFilter() {
		return new Filter() {
			
			@Override
			protected FilterResults performFiltering(CharSequence prefix) {
				FilterResults results = new FilterResults();
				
				if (tips == null || tips.isEmpty()) {
					results.values = null;
					results.count = 0;
					return results;
				}
				
				if (TextUtils.isEmpty(prefix)) {
					ArrayList<Tip> temp;
					synchronized (mLock) {
						temp = new ArrayList<Tip>(tips);
					}
					
					results.values = temp;
					results.count = temp.size();
				} else {
					
					String prefixString = prefix.toString().toLowerCase();
					
					ArrayList<Tip> values;
					synchronized (mLock) {
						values = new ArrayList<Tip>(tips);
					}
					final int count = values.size();
					final ArrayList<Tip> newValues = new ArrayList<Tip>();
					
					for (int i = 0; i < count; i++) {
						final Tip value = values.get(i);
						final String valueText = value.toString().toLowerCase();
						
						// First match against the whole, non-splitted value
						if (valueText.startsWith(prefixString)) {
							newValues.add(value);
						} else {
							final String[] words = valueText.split(" ");
							final int wordCount = words.length;
							
							// Start at index 0, in case valueText starts with
							// space(s)
							for (int k = 0; k < wordCount; k++) {
								if (words[k].startsWith(prefixString)) {
									newValues.add(value);
									break;
								}
							}
						}
					}
					
					results.values = newValues;
					results.count = newValues.size();
					
				}
				
				return results;
			}
			
			@Override
			protected void publishResults(CharSequence constraint,
					FilterResults results) {
				// TODO Auto-generated method stub
				tips = (List<Tip>) results.values;
				
				if (results.count > 0) {
					SuggestTipsAdapter.this.notifyDataSetChanged();
				} else {
					SuggestTipsAdapter.this.notifyDataSetInvalidated();
				}
			}
			
			@Override
			public CharSequence convertResultToString(Object resultValue) {
				
				if (resultValue == null)
					return null;
				
				Tip tip = (Tip) resultValue;
				return tip.getName();
			}
		};
	}
	
	static class TipViewHolder {
		
		public TextView mTipView;
	}
}
