package com.redfrog.traffic.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.redfrog.traffic.util.StringUtil;
import com.redfrog.traffic.util.SystemMetric;
import com.redfrog.traffic.util.TimeUtil;

//_______
//_____________________________________________________
//____________________
//_______________________________________________________________________________________________
//___________________________________
public abstract class EntityGeneral implements Serializable {

  //_____
  //_____________________________________________________
  private Long idSql;

  //__________________________
  //______________________________________________
  protected final String idJava;

  //__________________________
  //______________________________________________
  protected final Long creationTime;

  protected transient static final AtomicLong seqNumAtom = new AtomicLong(0L);

  public final Class<? extends EntityGeneral> clazz_sqlDebug = this.getClass();

  public EntityGeneral() {
    Instant creationTimeInstant = Instant.now();
    creationTime = TimeUnit.SECONDS.toNanos(creationTimeInstant.getEpochSecond()) + creationTimeInstant.getNano();
    long seqNum = seqNumAtom.incrementAndGet();
    idJava = String.format("%s--%s--%d", TimeUtil.time2strnano(SystemMetric.appBootTimeInstant), TimeUtil.time2strnano(creationTimeInstant), seqNum);
  }

  public Long getIdSql() { return idSql; }

  public void setIdSql(Long idSql) { this.idSql = idSql; }

  public String getIdJava() { return idJava; }

  public Long getCreationTime() { return creationTime; }

  public static AtomicLong getSeqnumatom() { return seqNumAtom; }

  @Override
  public String toString() { return "‘" + super.toString() + " :: idSql=" + idSql + " :: " + StringUtil.omitString(idJava, 35, 10) + "’"; }

}
