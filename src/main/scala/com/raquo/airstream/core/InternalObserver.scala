package com.raquo.airstream.core

import scala.util.{Failure, Success, Try}

trait InternalObserver[-A] {

  /** Must not throw */
  protected[airstream] def onNext(nextValue: A, transaction: Transaction): Unit

  /** Must not throw */
  protected[airstream] def onError(nextError: Throwable, transaction: Transaction): Unit

  /** Must not throw */
  protected[airstream] def onTry(nextValue: Try[A], transaction: Transaction): Unit
}

object InternalObserver {

  def apply[A](
    onNext: (A, Transaction) => Unit,
    onError: (Throwable, Transaction) => Unit
  ): InternalObserver[A] = {
    val onNextParam = onNext // It's beautiful on the outside
    val onErrorParam = onError

    new InternalObserver[A] {

      override final def onNext(nextValue: A, transaction: Transaction): Unit = {
        onNextParam(nextValue, transaction)
      }

      override final def onError(nextError: Throwable, transaction: Transaction): Unit = {
        onErrorParam(nextError, transaction)
      }

      override final def onTry(nextValue: Try[A], transaction: Transaction): Unit = {
        nextValue.fold(onError(_, transaction), onNext(_, transaction))
      }
    }
  }

  def fromTry[A](onTry: (Try[A], Transaction) => Unit): InternalObserver[A] = {
    val onTryParam = onTry // It's beautiful on the outside

    new InternalObserver[A] {

      override final def onNext(nextValue: A, transaction: Transaction): Unit = {
        onTry(Success(nextValue), transaction)
      }

      override final def onError(nextError: Throwable, transaction: Transaction): Unit = {
        onTry(Failure(nextError), transaction)
      }

      override final def onTry(nextValue: Try[A], transaction: Transaction): Unit = {
        onTryParam(nextValue, transaction)
      }
    }
  }
}
