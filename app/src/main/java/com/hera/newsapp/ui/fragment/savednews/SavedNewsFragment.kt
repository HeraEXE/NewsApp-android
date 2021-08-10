package com.hera.newsapp.ui.fragment.savednews

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.hera.newsapp.R
import com.hera.newsapp.data.model.Article
import com.hera.newsapp.databinding.FragmentSavedNewsBinding
import com.hera.newsapp.ui.MainActivity
import com.hera.newsapp.ui.MainViewModel
import com.hera.newsapp.ui.adapter.NewsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SavedNewsFragment : Fragment(R.layout.fragment_saved_news), NewsAdapter.Listener {

    private lateinit var viewModel: MainViewModel
    private val newsAdapter = NewsAdapter(this)
    private var _binding: FragmentSavedNewsBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = (activity as MainActivity).mainViewModel
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSavedNewsBinding.bind(view)
        initRecyclerView()
        observeSavedArticles()
    }


    private fun initRecyclerView() {
        binding.rvSavedNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            ItemTouchHelper(itemTouchHelper).attachToRecyclerView(this)
        }
    }


    private fun observeSavedArticles() = lifecycleScope.launch {
        viewModel.apply {
            allSavedArticlesFlow.collect { articles ->
                newsAdapter.differ.submitList(articles)
            }
        }
    }

    private val itemTouchHelper = object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START or ItemTouchHelper.END) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val article = newsAdapter.differ.currentList[viewHolder.bindingAdapterPosition]
            viewModel.delete(article)
            Snackbar.make(binding.root, "Article was deleted", Snackbar.LENGTH_LONG)
                .setAction("UNDO") {
                    viewModel.insert(article)
                }
                .show()
        }
    }


    override fun onItemClick(article: Article) {
        val action = SavedNewsFragmentDirections.actionSavedNewsFragmentToArticleFragment(article)
        findNavController().navigate(action)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}