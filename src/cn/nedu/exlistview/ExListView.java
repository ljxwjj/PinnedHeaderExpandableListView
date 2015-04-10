package cn.nedu.exlistview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

public class ExListView extends Activity {
	private static final String G_TEXT = "g_text";
	
	private static final String C_TEXT1 = "c_text1";
	private static final String C_TEXT2 = "c_text1";
	   
    List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
    List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();

    MyExpandableListAdapter adapter;
    PinnedHeaderExpListView exList;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mPinnedHeaderBackgroundColor = getResources().getColor(android.R.color.black);
        mPinnedHeaderTextColor = getResources().getColor(android.R.color.white);

        
        for (int i = 0; i < 5; i++) {
            Map<String, String> curGroupMap = new HashMap<String, String>();
            groupData.add(curGroupMap);
            curGroupMap.put(G_TEXT, "Group " + i);
              
            List<Map<String, String>> children = new ArrayList<Map<String, String>>();
            for (int j = 0; j < 5; j++) {
                Map<String, String> curChildMap = new HashMap<String, String>();
                children.add(curChildMap);
                curChildMap.put(C_TEXT1, "Child " + j);
                curChildMap.put(C_TEXT2, "Child " + j);
            }
            childData.add(children);
        }
        
        adapter=new MyExpandableListAdapter(ExListView.this);
        exList = (PinnedHeaderExpListView) findViewById(R.id.list);
		exList.setAdapter(adapter);
		exList.setGroupIndicator(null);
		exList.setDivider(null);
		View h = LayoutInflater.from(this).inflate(R.layout.member_listview, null, false);
		exList.setPinnedHeaderView(h);
		exList.setOnScrollListener(adapter);
    }
    
    
    private int mPinnedHeaderBackgroundColor;
    private int mPinnedHeaderTextColor;

    
    class MyExpandableListAdapter extends AbstractPinnedHeaderAdapter {
    	ExListView exlistview;

    	public MyExpandableListAdapter(ExListView elv) {
    		super();
    		exlistview = elv;
    	}
    	public View getGroupView(int groupPosition, boolean isExpanded,
    			View convertView, ViewGroup parent) {
	
			View view = convertView;
			if (view == null) {
				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.member_listview, null);
			}

				TextView title = (TextView) view.findViewById(R.id.content_001);
				title.setText(getGroup(groupPosition).toString());

				ImageView image=(ImageView) view.findViewById(R.id.tubiao);
				if(isExpanded)
					image.setBackgroundResource(R.drawable.btn_browser2);
				else image.setBackgroundResource(R.drawable.btn_browser);
				
    		return view;
    	}


    	public long getGroupId(int groupPosition) {
    		return groupPosition;
    	}
    	
    	public Object getGroup(int groupPosition) {
    		return groupData.get(groupPosition).get(G_TEXT).toString();
    	}

    	public int getGroupCount() {
			return groupData.size();

    	}
    	//**************************************
    	public View getChildView(int groupPosition, int childPosition,
    			boolean isLastChild, View convertView, ViewGroup parent) {
    		View view = convertView;
			if (view == null) {
				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.member_childitem, null);	
			}
			   	final TextView title = (TextView) view.findViewById(R.id.child_text);
					title.setText(childData.get(groupPosition).get(childPosition).get(C_TEXT1).toString());			
				final TextView title2 = (TextView) view.findViewById(R.id.child_text2);
					title2.setText(childData.get(groupPosition).get(childPosition).get(C_TEXT2).toString());
			 
			return view;
    	}

    	public long getChildId(int groupPosition, int childPosition) {
    		return childPosition;
    	}
    	
    	public Object getChild(int groupPosition, int childPosition) {
    		return childData.get(groupPosition).get(childPosition).get(C_TEXT1).toString();
    	}

    	public int getChildrenCount(int groupPosition) {
    		return childData.get(groupPosition).size();
    	}
    	//**************************************
    	public boolean hasStableIds() {
    		return true;
    	}

    	public boolean isChildSelectable(int groupPosition, int childPosition) {
    		return true;
    	}
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			if (view instanceof PinnedHeaderExpListView) {
                ((PinnedHeaderExpListView) view).configureHeaderView(firstVisibleItem);
            }

			
		}
		@Override
		public void configurePinnedHeader(View v, int position, int alpha,
				boolean isExpanded) {
			TextView title = (TextView) v.findViewById(R.id.content_001);
			title.setText(getGroup(position).toString());
			
			if (alpha == 255 || alpha == 0) {
				if (isExpanded) {
//					title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_up, 0);
				} else {
//					title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_down, 0);
				}
			}
		}
		

    }
}