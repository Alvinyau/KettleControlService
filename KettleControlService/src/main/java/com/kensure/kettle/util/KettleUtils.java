package com.kensure.kettle.util;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleEOFException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.filerep.KettleFileRepository;
import org.pentaho.di.repository.filerep.KettleFileRepositoryMeta;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;
import org.pentaho.di.repository.kdr.KettleDatabaseRepositoryMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import com.kensure.kettle.model.DbRepParams;

/**
 * 
 * @ClassName: KettleUtils
 * @Description: Kettle������
 * @author anduo
 * @date 2015��1��22�� ����10:27:14
 *
 */
public class KettleUtils {

  /**
   * @title initKettleFileRepository
   * @description ��ʼ��һ��kettle�ļ���Դ��
   * @param dir
   * @return KettleFileRepository
   * @throws KettleException
   */
  public static KettleFileRepository initFileRepository(String dir) throws KettleException {
    KettleFileRepository repository = null;
    // ��ʼ��
    KettleEnvironment.init();
    // ��Դ��Ԫ����
    KettleFileRepositoryMeta repMeta =
        new KettleFileRepositoryMeta("", "", "���ݲɼ�", "file:///" + dir);
    // �ļ���ʽ����Դ��
    repository = new KettleFileRepository();
    repository.init(repMeta);
    return repository;
  }


  public static KettleDatabaseRepository initDatabaseRepository(DbRepParams params)
      throws KettleException {
    KettleDatabaseRepository repository = null;
    // ��ʼ��
    KettleEnvironment.init();
    DatabaseMeta databaseMeta =
        new DatabaseMeta(params.getName(), params.getType(), params.getAccess(), params.getHost(),
            params.getDb(), params.getPort(), params.getUser(), params.getPass());
    KettleDatabaseRepositoryMeta repositoryMeta = new KettleDatabaseRepositoryMeta();
    repositoryMeta.setConnection(databaseMeta);
    repository = new KettleDatabaseRepository();
    repository.init(repositoryMeta);
    repository.connect(params.getUsername(), params.getPassword());

    RepositoryDirectoryInterface dir = new RepositoryDirectory();
    dir.setObjectId(repository.getRootDirectoryID());
    return repository;

  }

  public static Trans executeTrans(String transname, Object rep) throws Exception {
    return executeTrans(transname, rep, true);
  }

  /**
   * @Title: executeTrans
   * @Description: ִ��ת��
   * @param transname
   * @param rep
   * @throws Exception
   */
  public static Trans executeTrans(String transname, Object rep, boolean isWaitUntilFinished)
      throws Exception {
    // ת������
    Trans trans = null;
    if (transname != null && !"".equals(transname)) {
      TransMeta transMeta = null;
      if (rep instanceof KettleFileRepository) {
        KettleFileRepository repository = (KettleFileRepository) rep;
        transMeta =
            repository.loadTransformation(repository.getTransformationID(transname, null), null);
      } else if (rep instanceof KettleDatabaseRepository) {
        KettleDatabaseRepository repository = (KettleDatabaseRepository) rep;
        RepositoryDirectoryInterface dir = new RepositoryDirectory();
        dir.setObjectId(repository.getRootDirectoryID());
        transMeta =
            repository.loadTransformation(repository.getTransformationID(transname, dir), null);
      }
      // ת��
      trans = new Trans(transMeta);
      // ִ��ת��
      trans.execute(null);

      // �ȴ�ת��ִ�н���
      if (isWaitUntilFinished) {
        trans.waitUntilFinished();
      }
      // �׳��쳣
      if (trans.getErrors() > 0) {
        trans.stopAll();
        throw new Exception("��������з����쳣");
      }
    } else {
      throw new KettleEOFException("������Ϊ��!");
    }
    return trans;
  }


}