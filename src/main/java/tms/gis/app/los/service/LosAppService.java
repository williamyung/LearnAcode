package tms.gis.app.los.service;

import protocol.entity.LocalDetailSearch;
import protocol.entity.LocalSearch;
import protocol.entity.LosPoiDetailInfo;
import protocol.entity.LosPoiInfos;
import tms.gis.biz.svc.vo.ProtocolHeaderInfoVO;



/**

adfkjdkf

lfdklasfdldksf william change


 * Statement
 *
 * <pre>
 * ClassName   : LosAppService.java
 * Scenario    : 
 * Modification Information
 *
 *     since          author             description
 *  ===========    =============    ===========================
 *  Jan 23, 2013   kwihyun.ko       First Gen...
 * </pre>
 * @author kwihyun.ko
 * @since Jan 23, 2013
 * @version 1.0
 */
public interface LosAppService {


    /**
     * Statement
     *
     * <pre>
     * Message ID  : 
     * Modification Information
     *
     *     since          author              description
     *  ===========    =============    ===========================
     *  Jan 23, 2013   kwihyun.ko       First Gen...
     * </pre>
     * @author kwihyun.ko
     * @since Jan 23, 2013
     * @version 1.0
     * 
     * @param protocolHeaderInfoVO
     * @param los01ServiceTwowayVO
     * @param recevedLosInfo
     * @throws Exception
     */
//    Due to the often request also Big. and never use this data anymore.
//    public void insertLos01(ProtocolHeaderInfoVO protocolHeaderInfoVO,
//            ServiceTwowayVO los01ServiceTwowayVO, LosInfo recevedLosInfo) throws Exception;



    /**
     * Insert LOS-02 Message
     *
     * <pre>
     * Message ID  : LOS-02
     * Modification Information
     *
     *     since          author              description
     *  ===========    =============    ===========================
     *  Jan 23, 2013   kwihyun.ko       First Gen...
     * </pre>
     * @author kwihyun.ko
     * @since Jan 23, 2013
     * @version 1.0
     * 
     * @param protocolHeaderInfoVO
     * @param los02ServiceTwowayVO
     * @param losDetailInfo
     * @throws Exception
     */
//    
//    public void insertLos02(ProtocolHeaderInfoVO protocolHeaderInfoVO,
//            ServiceTwowayVO los02ServiceTwowayVO, LosDetailInfo losDetailInfo) throws Exception;



    /**
     * Statement
     *
     * <pre>
     * Message ID  : 
     * Modification Information
     *
     *     since          author              description
     *  ===========    =============    ===========================
     *  Jan 23, 2013   kwihyun.ko       First Gen...
     * </pre>
     * @author kwihyun.ko
     * @since Jan 23, 2013
     * @version 1.0
     * 
     * @param protocolHeaderInfoVO
     * @param localSearch
     * @return
     * @throws Exception
     */
    public LosPoiInfos processLocalSearchToLosPoiInfos(ProtocolHeaderInfoVO protocolHeaderInfoVO,
            LocalSearch localSearch) throws Exception;


    /**
     * Statement
     *
     * <pre>
     * Message ID  : 
     * Modification Information
     *
     *     since          author              description
     *  ===========    =============    ===========================
     *  Jan 23, 2013   kwihyun.ko       First Gen...
     * </pre>
     * @author kwihyun.ko
     * @since Jan 23, 2013
     * @version 1.0
     */
    public LosPoiDetailInfo processLocalDetailSearchToLosPoiDetailInfo(
            ProtocolHeaderInfoVO protocolHeaderInfoVO, LocalDetailSearch localDetailSearch) throws Exception;



}
