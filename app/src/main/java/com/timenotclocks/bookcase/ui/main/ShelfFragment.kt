package com.timenotclocks.bookcase.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.timenotclocks.bookcase.*


/**
 * A placeholder fragment containing a simple view.
 */
class ShelfFragment : Fragment() {

    private lateinit var pageViewModel: PageViewModel
    private val bookViewModel: BookViewModel by viewModels {
        BookViewModelFactory((activity?.application as BooksApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProvider(this).get(PageViewModel::class.java).apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
        }

    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_main, container, false)
        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerview)
        val adapter = BookListAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)


        when(pageViewModel.getIndex()) {
            1 -> {
                bookViewModel.currentShelf.observe(viewLifecycleOwner) { books ->
                    // Update the cached copy of the books in the adapter.
                    books.let { adapter.submitList(it) }
                }
            }
            2 -> {
                bookViewModel.readShelf.observe(viewLifecycleOwner) { books ->
                    // Update the cached copy of the books in the adapter.
                    books.let { adapter.submitList(it) }
                }
            }
            3 -> {
                bookViewModel.toReadShelf.observe(viewLifecycleOwner) { books ->
                    // Update the cached copy of the books in the adapter.
                    books.let { adapter.submitList(it) }
                }
            }
            else -> {
                Log.e("BK","Some random tab was selected woops")
            }
        }

        return root
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): ShelfFragment {
            return ShelfFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }
}