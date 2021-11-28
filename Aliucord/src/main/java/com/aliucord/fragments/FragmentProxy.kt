/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */
package com.aliucord.fragments

import android.animation.Animator
import android.app.Activity
import android.content.*
import android.content.IntentSender.SendIntentException
import android.content.res.Configuration
import android.os.Bundle
import android.util.AttributeSet
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.view.animation.Animation
import androidx.core.app.SharedElementCallback
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import com.aliucord.Main
import com.discord.app.AppComponent
import rx.subjects.Subject
import java.io.FileDescriptor
import java.io.PrintWriter
import java.util.*

open class FragmentProxy : Fragment(), AppComponent {
    companion object {
        val fragments: MutableMap<String, Fragment> = HashMap()
    }

    private var mFragment: Fragment? = null

    //    @Override
    //    public void onViewBound(View view) {
    //        if (mFragment != null) mFragment.onViewBound(view);
    //        super.onViewBound(view);
    //    }

    override fun getUnsubscribeSignal(): Subject<Void, Void>? {
        val fragment = getmFragment()
        return if (fragment is AppComponent) (fragment as AppComponent).unsubscribeSignal else null
    }

    override fun getLifecycle() =
        getmFragment()!!.lifecycle

    override fun getViewLifecycleOwner() =
        getmFragment()!!.viewLifecycleOwner


    override fun getViewLifecycleOwnerLiveData() =
        getmFragment()!!.viewLifecycleOwnerLiveData


    override fun setArguments(args: Bundle?) {
        getmFragment()!!.arguments = args
    }

    override fun setInitialSavedState(state: SavedState?) {
        getmFragment()!!.setInitialSavedState(state)
    }

    override fun setTargetFragment(fragment: Fragment?, requestCode: Int) {
        getmFragment()!!.setTargetFragment(fragment, requestCode)
    }

    override fun getContext() =
        getmFragment()!!.context

    override fun onHiddenChanged(hidden: Boolean) {
        getmFragment()!!.onHiddenChanged(hidden)
    }

    override fun setRetainInstance(retain: Boolean) {
        getmFragment()!!.retainInstance = retain
    }

    override fun setHasOptionsMenu(hasMenu: Boolean) {
        getmFragment()!!.setHasOptionsMenu(hasMenu)
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        getmFragment()!!.setMenuVisibility(menuVisible)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        getmFragment()!!.userVisibleHint = isVisibleToUser
    }

    override fun getUserVisibleHint(): Boolean {
        return getmFragment()!!.userVisibleHint
    }

    override fun getLoaderManager(): LoaderManager {
        return getmFragment()!!.loaderManager
    }

    override fun startActivity(intent: Intent) {
        getmFragment()!!.startActivity(intent)
    }

    override fun startActivity(intent: Intent, options: Bundle?) {
        getmFragment()!!.startActivity(intent, options)
    }

    override fun startActivityForResult(intent: Intent, requestCode: Int) {
        getmFragment()!!.startActivityForResult(intent, requestCode)
    }

    override fun startActivityForResult(intent: Intent, requestCode: Int, options: Bundle?) {
        getmFragment()!!.startActivityForResult(intent, requestCode, options)
    }

    @Throws(SendIntentException::class)
    override fun startIntentSenderForResult(
        intent: IntentSender,
        requestCode: Int,
        fillInIntent: Intent?,
        flagsMask: Int,
        flagsValues: Int,
        extraFlags: Int,
        options: Bundle?
    ) {
        getmFragment()!!.startIntentSenderForResult(
            intent,
            requestCode,
            fillInIntent,
            flagsMask,
            flagsValues,
            extraFlags,
            options
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        getmFragment()!!.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        getmFragment()!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun shouldShowRequestPermissionRationale(permission: String) =
        getmFragment()!!.shouldShowRequestPermissionRationale(permission)

    //    @NonNull
    //    @Override
    //    public LayoutInflater onGetLayoutInflater(@Nullable Bundle savedInstanceState) {
    ////        return mFragment.onGetLayoutInflater(savedInstanceState);
    //        try {
    //            return super.onGetLayoutInflater(savedInstanceState);
    //        } catch (Exception e) {
    //            return null;
    //        }
    //    }
    //
    //    @NonNull
    //    @Override
    //    @SuppressLint("RestrictedApi")
    //    public LayoutInflater getLayoutInflater(@Nullable Bundle savedFragmentState) {
    //        return mFragment.getLayoutInflater(savedFragmentState);
    //    }
    override fun onInflate(context: Context, attrs: AttributeSet, savedInstanceState: Bundle?) {
        getmFragment()!!.onInflate(context, attrs, savedInstanceState)
        super.onInflate(context, attrs, savedInstanceState)
    }

    override fun onInflate(activity: Activity, attrs: AttributeSet, savedInstanceState: Bundle?) {
        getmFragment()!!.onInflate(activity, attrs, savedInstanceState)
        super.onInflate(activity, attrs, savedInstanceState)
    }

    override fun onAttachFragment(childFragment: Fragment) {
        getmFragment()!!.onAttachFragment(childFragment)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getmFragment()!!.onAttach(context)
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        getmFragment()!!.onAttach(activity)
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        return getmFragment()!!.onCreateAnimation(transit, enter, nextAnim)
    }

    override fun onCreateAnimator(transit: Int, enter: Boolean, nextAnim: Int): Animator? {
        return getmFragment()!!.onCreateAnimator(transit, enter, nextAnim)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (mFragment != null) mFragment!!.onCreate(savedInstanceState)
        super.onCreate(savedInstanceState)
    }

    private var mView: View? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragment = getmFragment()
        try {
            val field = Fragment::class.java.getDeclaredField("mFragmentManager").apply { isAccessible = true }
            field[fragment] = fragmentManager
            val hostField = Fragment::class.java.getDeclaredField("mHost").apply { isAccessible = true }
            hostField[fragment] = hostField[this]
        } catch (e: Exception) {
            Main.logger.error(e)
        }
        mView = fragment!!.onCreateView(inflater, container, savedInstanceState)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        getmFragment()!!.onViewCreated(view, savedInstanceState)
    }

    override fun getView() = getmFragment()!!.view ?: mView


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        getmFragment()!!.onActivityCreated(savedInstanceState)
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        getmFragment()!!.onViewStateRestored(savedInstanceState)
        super.onViewStateRestored(savedInstanceState)
    }

    override fun onStart() {
        getmFragment()!!.onStart()
        super.onStart()
    }

    override fun onResume() {
        getmFragment()!!.onResume()
        super.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        getmFragment()!!.onSaveInstanceState(outState)
    }

    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean) {
        getmFragment()!!.onMultiWindowModeChanged(isInMultiWindowMode)
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        getmFragment()!!.onPictureInPictureModeChanged(isInPictureInPictureMode)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        getmFragment()!!.onConfigurationChanged(newConfig)
        super.onConfigurationChanged(newConfig)
    }

    override fun onPrimaryNavigationFragmentChanged(isPrimaryNavigationFragment: Boolean) {
        getmFragment()!!.onPrimaryNavigationFragmentChanged(isPrimaryNavigationFragment)
    }

    override fun onPause() {
        getmFragment()!!.onPause()
        super.onPause()
    }

    override fun onStop() {
        getmFragment()!!.onStop()
        super.onStop()
    }

    override fun onLowMemory() {
        getmFragment()!!.onLowMemory()
        super.onLowMemory()
    }

    override fun onDestroyView() {
        getmFragment()!!.onDestroyView()
        super.onDestroyView()
    }

    override fun onDestroy() {
        getmFragment()!!.onDestroy()
        super.onDestroy()
    }

    override fun onDetach() {
        getmFragment()!!.onDetach()
        super.onDetach()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        getmFragment()!!.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        getmFragment()!!.onPrepareOptionsMenu(menu)
    }

    override fun onDestroyOptionsMenu() {
        getmFragment()!!.onDestroyOptionsMenu()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return getmFragment()!!.onOptionsItemSelected(item)
    }

    override fun onOptionsMenuClosed(menu: Menu) {
        getmFragment()!!.onOptionsMenuClosed(menu)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        getmFragment()!!.onCreateContextMenu(menu, v, menuInfo)
    }

    override fun registerForContextMenu(view: View) {
        getmFragment()!!.registerForContextMenu(view)
    }

    override fun unregisterForContextMenu(view: View) {
        getmFragment()!!.unregisterForContextMenu(view)
    }

    override fun onContextItemSelected(item: MenuItem) =
        getmFragment()!!.onContextItemSelected(item)

    override fun setEnterSharedElementCallback(callback: SharedElementCallback?) {
        getmFragment()!!.setEnterSharedElementCallback(callback)
    }

    override fun setExitSharedElementCallback(callback: SharedElementCallback?) {
        getmFragment()!!.setExitSharedElementCallback(callback)
    }

    override fun setEnterTransition(transition: Any?) {
        getmFragment()!!.enterTransition = transition
    }

    override fun getEnterTransition() =
        getmFragment()!!.enterTransition

    override fun setReturnTransition(transition: Any?) {
        getmFragment()!!.returnTransition = transition
    }

    override fun getReturnTransition() =
        getmFragment()!!.returnTransition

    override fun setExitTransition(transition: Any?) {
        getmFragment()!!.exitTransition = transition
    }

    override fun getExitTransition() =
        getmFragment()!!.exitTransition

    override fun setReenterTransition(transition: Any?) {
        getmFragment()!!.reenterTransition = transition
    }

    override fun getReenterTransition() =
        getmFragment()!!.reenterTransition

    override fun setSharedElementEnterTransition(transition: Any?) {
        getmFragment()!!.sharedElementEnterTransition = transition
    }

    override fun getSharedElementEnterTransition() =
        getmFragment()!!.sharedElementEnterTransition

    override fun setSharedElementReturnTransition(transition: Any?) {
        getmFragment()!!.sharedElementReturnTransition = transition
    }

    override fun getSharedElementReturnTransition() =
        getmFragment()!!.sharedElementReturnTransition

    override fun setAllowEnterTransitionOverlap(allow: Boolean) {
        getmFragment()!!.allowEnterTransitionOverlap = allow
    }

    override fun getAllowEnterTransitionOverlap() =
        getmFragment()!!.allowEnterTransitionOverlap

    override fun setAllowReturnTransitionOverlap(allow: Boolean) {
        getmFragment()!!.allowReturnTransitionOverlap = allow
    }

    override fun getAllowReturnTransitionOverlap() =
        getmFragment()!!.allowReturnTransitionOverlap

    override fun postponeEnterTransition() {
        getmFragment()!!.postponeEnterTransition()
    }

    override fun startPostponedEnterTransition() =
        getmFragment()!!.startPostponedEnterTransition()

    override fun dump(
        prefix: String,
        fd: FileDescriptor?,
        writer: PrintWriter,
        args: Array<String>?
    ) = getmFragment()!!.dump(prefix, fd, writer, args)

    open fun getmFragment(): Fragment? {
        if (mFragment == null) {
            if (arguments != null) {
                val id = requireArguments().getString("AC_FRAGMENT_ID")
                mFragment = fragments[id]
                fragments.remove(id)
            }
        }
        return mFragment
    }
}
