package br.com.bpelogia.viewcustom.rx

import com.miguelcatalan.materialsearchview.MaterialSearchView

import rx.Observable
import rx.subjects.BehaviorSubject

/**
 * @author Bruno Pelogia
 * @since 09/01/2018
 */
object RxView {

    fun fromSearchView(searchView: MaterialSearchView): Observable<String> {
        val subject = BehaviorSubject.create("")

        searchView.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {
                searchView.hideKeyboard(searchView)
                subject.onNext(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (!newText.isEmpty()) {
                    subject.onNext(newText)
                }
                return true
            }
        })

        return subject
    }

    fun requestOnce(countCalls: Int): Observable<Int> {
        val subject = BehaviorSubject.create(countCalls)
        subject.onNext(countCalls)
        return subject
    }
}