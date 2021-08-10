package com.hera.newsapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hera.newsapp.data.model.Article
import com.hera.newsapp.databinding.ItemNewsBinding
import com.hera.newsapp.util.extention.loadUrl

class NewsAdapter(
    private val listener: Listener
    ) : RecyclerView.Adapter<NewsAdapter.ViewHolder>() {

    interface Listener {

        fun onItemClick(article: Article)
    }


    inner class ViewHolder(
        private val binding: ItemNewsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(position: Int) {
            val article = differ.currentList[position]
            article.apply {
                binding.apply {
                    urlToImage?.let { ivPreviewNews.loadUrl(urlToImage) }
                    title?.let { tvTitleNews.text = it }
                    source?.let { source -> source.name?.let { tvEditorNews.text = it } }
                    author?.let { tvAuthorNews.text = it }
                    publishedAt?.let { tvDateNews.text = it }
                    clItemNews.setOnClickListener { listener.onItemClick(article) }
                }
            }
        }
    }


    private val diffCallback = object : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article) = oldItem.url == newItem.url
        override fun areContentsTheSame(oldItem: Article, newItem: Article) = oldItem == newItem
    }
    val differ = AsyncListDiffer(this, diffCallback)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemNewsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )


    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.onBind(position)


    override fun getItemCount() = differ.currentList.size
}