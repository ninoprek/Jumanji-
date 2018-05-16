package jumanji.sda.com.jumanji

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


class CommunityFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_community, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
    }
}

class CommunityAdapter: RecyclerView.Adapter<CommunityHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_community_meetup, parent, false)
        return CommunityHolder(view)
    }

    override fun getItemCount(): Int {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return 10
    }

    override fun onBindViewHolder(holder: CommunityHolder, position: Int) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

class CommunityHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

}