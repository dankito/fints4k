package net.dankito.banking.persistence.dao

import androidx.room.Transaction
import net.dankito.banking.persistence.model.*


/*      Room didn't allow me to add these methods to BaseDao directly (Kapt error), so i defined them as extension methods      */

@Transaction
fun <T> BaseDao<T>.saveOrUpdate(obj: T) {
    val id = insert(obj)

    if (wasNotInserted(id)) {
        update(obj)
    }
    else {
        setId(obj, id)
    }
}

@Transaction
fun <T> BaseDao<T>.saveOrUpdate(objList: List<T>) {
    val ids = insert(objList)

    // i was not allowed to use mapIndexedNotNull()
    val notInsertedObjects = mutableListOf<T>()
    ids.forEachIndexed { index, id ->
        val obj = objList[index]

        if (wasNotInserted(id)) {
            notInsertedObjects.add(obj)
        }
        else {
            setId(obj, id)
        }
    }

    update(notInsertedObjects)
}

private fun wasNotInserted(id: Long): Boolean {
    return id == BaseDao.ObjectNotInsertedId
}

private fun <T> setId(obj: T, id: Long) {
    if (obj is Bank) {
        obj.id = id // why doesn't Room set this on itself?
        obj.technicalId = obj.id.toString()
    }
    else if (obj is BankAccount) {
        obj.id = id // why doesn't Room set this on itself?
        obj.technicalId = obj.id.toString()
    }
    else if (obj is AccountTransaction) {
        obj.id = id // why doesn't Room set this on itself?
        obj.technicalId = obj.id.toString()
    }
}