package com.redfrog.traffic.util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class MathUtil {

  @SuppressWarnings("rawtypes")
  private static BiPredicate NullBiPredicate = (t, u) -> true;
  @SuppressWarnings("rawtypes")
  private static Predicate NullPredicate = (t) -> true;

  public static <T> LinkedList<LinkedList<T>> get_Permuataion(Collection<T> array_in) {
    LinkedList<LinkedList<T>> perm = new LinkedList<>();
    LinkedList<T> arr_Rest_next = new LinkedList<T>(array_in);
    //___________________________________________
    //_____________________________________________________________________
    //_________________________________________
    //_______________________________________________________________________
    //_____
    get_Permuataion_recursive(null, arr_Rest_next, perm, false, NullBiPredicate, NullBiPredicate);
    return perm;
  }

  public static <T> LinkedList<LinkedList<T>> get_Permuataion_Scattered(Collection<T> array_in) {
    LinkedList<LinkedList<T>> perm = new LinkedList<>();
    LinkedList<T> arr_Rest_next = new LinkedList<T>(array_in);
    get_Permuataion_recursive(null, arr_Rest_next, perm, true, NullBiPredicate, NullBiPredicate);
    return perm;
  }

  public static <T> LinkedList<LinkedList<T>> get_Permuataion_Scattered(Collection<T> array_in, BiPredicate<LinkedList<T>, T> pred_AbandonCurrPath, BiPredicate<LinkedList<T>, T> pred_AbandonAfterCurrPath) {
    LinkedList<LinkedList<T>> perm = new LinkedList<>();
    LinkedList<T> arr_Rest_next = new LinkedList<T>(array_in);
    get_Permuataion_recursive(null, arr_Rest_next, perm, true, pred_AbandonCurrPath, pred_AbandonAfterCurrPath);
    return perm;
  }

  //______________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________
  //________________________________
  //_________________________________
  //_____
  //______________________________________________________________________
  //____________________________________________________________________________________________________________________________________________________
  //_____________________________________________________________________________________________________________________________________________________________________________________________________________________________________

  //___________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________
  //_______________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________
  private static <T> void get_Permuataion_recursive(LinkedList<T> arr_BuildOn_prev, LinkedList<T> arr_Rest_curr, LinkedList<LinkedList<T>> perm, final boolean mode_Scattered,
                                                    final BiPredicate<LinkedList<T>, T> pred_AbandonCurrPath, final BiPredicate<LinkedList<T>, T> pred_AbandonAfterCurrPath) {

    int i = -1; 
    boolean det_LastItem = arr_Rest_curr.size() == 1;
    for (T obj_curr : arr_Rest_curr) {
      i++;
      //
      if (pred_AbandonCurrPath != NullBiPredicate && pred_AbandonCurrPath.test(arr_BuildOn_prev, obj_curr)) { continue; }

      //
      LinkedList<T> arr_BuildOn_curr;
      if (arr_BuildOn_prev == null) {
        arr_BuildOn_curr = new LinkedList<T>();
      }
      else {
        arr_BuildOn_curr = new LinkedList<T>(arr_BuildOn_prev);
      }
      arr_BuildOn_curr.add(obj_curr);
      if (mode_Scattered) {
        perm.add(arr_BuildOn_curr);
      }
      else if (det_LastItem) {
        perm.add(arr_BuildOn_curr); //__
      }

      //
      if (!det_LastItem) {
        //
        if (pred_AbandonAfterCurrPath != NullBiPredicate && pred_AbandonAfterCurrPath.test(arr_BuildOn_prev, obj_curr)) { continue; }
        
        LinkedList<T> arr_Rest_next = new LinkedList<T>(arr_Rest_curr);
        arr_Rest_next.remove(i);
        
        get_Permuataion_recursive(arr_BuildOn_curr, arr_Rest_next, perm, mode_Scattered, pred_AbandonCurrPath, pred_AbandonAfterCurrPath);
      }
    }
  }
}
