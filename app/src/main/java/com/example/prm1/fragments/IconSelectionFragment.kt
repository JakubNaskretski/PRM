package com.example.prm1.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prm1.Navigable
import com.example.prm1.adapters.TaskImagesAdapter
import com.example.prm1.databinding.FragmentIconSelectionBinding

class IconSelectionFragment : Fragment() {
    private lateinit var binding: FragmentIconSelectionBinding
    private lateinit var adapter: TaskImagesAdapter
    public var selectedIcon: Int?= null;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentIconSelectionBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = TaskImagesAdapter().newInstance(this)
        binding.images.apply {
            adapter = this@IconSelectionFragment.adapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    public fun iconSelected(iconNo: Int) {
        selectedIcon = iconNo
        (activity as? Navigable)?.navigate(Navigable.Destination.Edit)
    }


}