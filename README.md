## VeryVeryVeryEasySpring <br>
### 一个非常非常非常简陋的，实现Spring、SpringMVC主要注解的DEMO <br>

> 做这个DEMO的起因，并不是真的想实现什么Spring，主要是为了加深一下对spring、springMVC框架的理解，所以在网上搜了些资料，加上自己的理解，简单的实现了这个非常之简陋的框架 <br>

#### 简单使用介绍及实现功能： <br>
##### 主要功能 <br>
这个框架demo主要实现了Spring中@Component、@Autowired、@Aspect、@Ponitcut、@Before、@After <br>
    以及SpringMVC中@Controller、@RequestMapping、@RequestParam注解 <br>
采用了常用的IOC/AOP思想，实现了简单的控制反转、依赖注入以及访问映射 <br>

##### 简单使用 <br>
测试模块 <br>
HelloController，映射/hello路径，并接受参数name，返回一个hello+name <br>
![](https://github.com/luopoQAQ/VeryVeryVeryEasySpring/blob/master/test_img/hello.PNG)

运行结果 <br>
![](https://github.com/luopoQAQ/VeryVeryVeryEasySpring/blob/master/test_img/hello_google.PNG)

GoodController，映射/good路径，返回good night <br>
![](https://github.com/luopoQAQ/VeryVeryVeryEasySpring/blob/master/test_img/good.PNG)

运行结果 <br>
![](https://github.com/luopoQAQ/VeryVeryVeryEasySpring/blob/master/test_img/good_google.PNG)

同时调用了接口goodAfternoon的good()方法，它的实现类被切点标记，则会有goodMorning()的前置通知与goodEvening()的后置通知在切点方法前后被调用 <br>
![](https://github.com/luopoQAQ/VeryVeryVeryEasySpring/blob/master/test_img/qiemian.PNG)

Good接口及实现类 <br>
![](https://github.com/luopoQAQ/VeryVeryVeryEasySpring/blob/master/test_img/goodInterface.PNG)
![](https://github.com/luopoQAQ/VeryVeryVeryEasySpring/blob/master/test_img/goodInterfaceImpl.PNG)

切面类 <br>
![](https://github.com/luopoQAQ/VeryVeryVeryEasySpring/blob/master/test_img/aspect.PNG)

控制台打印bean的初始化流程： <br>
![](https://github.com/luopoQAQ/VeryVeryVeryEasySpring/blob/master/test_img/kongzhitai.PNG)

#### 主要思路 <br>
代码里注释很详细了，这里再把思路大体捋一下 <br>

> 运行项目中springFrameeorkTest模块build后的jar，会扫描启动类所在包下的所有类 <br>
>> 对扫秒结果classList进行处理，将需要处理的类进行分类，注解了@Aspect的类加入aspectClassList，注解了@Component和@Controller的类加入needCreateClassList <br>
>> 对needCreateClassList进行尝试创建bean的处理，直到所有bean都创建完毕，needCreateClassList为空 <br>
>>> 每次处理都是一个迭代，遍历needCreateClassList中所有的class，尝试创建其对应bean，如果创建成功，则加入待删除list：removeList <br>
>>>> bean的创建，首先是利用反射取得所有域值为空的该对象的实例，然后获取所有的域，依次检测该域是否有@AutoWired注解，如果有，则尝试注入相应bean <br>
>>>> 为待创建的bean注入依赖的过程： <br>
>>>>> 首先将其加入hasAutoWiredBeans list，方便在AOP处理后对依赖进行更新 <br>
>>>>> 域依赖bean的获取，首先检查在beans（已经创建好的bean的一个list）中是否存在需要的bean，如果存在则直接设置该域的依赖为此bean <br>
>>>>> 如果不存在，则检查该bean是否为借口，如果不是，则直接跳出迭代 <br>
>>>>> 如果该域是一个接口，则遍历beans，检测是否有其子类（实现类）的bean，如果存在，且唯一，则注入，否则跳出（说明该bean的创建再次失败）<br>
>>>>> 
>>>> 
>>> 遍历过程中，如果出现依赖注入的对象重复，即：如果被注入的域是一个接口，而它的实现bean有多个，则抛出异常，说明可以选择的bean不明确 <br>
>>> 遍历结束后，检查removeList是否为空，如果removeList中不存在待删除的class，即此次迭代中没有创建任何bean，那么bean的创建就失败了，可能的原因是需要注入bean的域未声明其bean，或者陷入循环依赖 <br>
>>> 否则的话，将removeList中元素一一取出，并将待创建bean list：needCreateClassList中对应class删除，直到带创建list为空 <br>
>>> 
>> 第一次bean的创建处理完毕 <br>
>> 检测aspectClassList是否为空，如果为空，则不需要进行AOPbean的再注入，否则进行如下处理 <br>
>> 对aspectClassList进行遍历，对切面类进行处理 <br>
>>> 得到该切面类的所有方法，进行遍历 <br>
>>> 第一次遍历，取得所有的切点，处理得到切点标明的被代理类及其方法 <br>
>>> 第二次遍历，取得所有的通知方法 <br>
>>> 利用JDK动态代理，通过代理类bean实现实现方法与目标方法的结合 <br>
>>> 对beans中被代理的类的bean进行更新 <br>
>>> 
>> AOP代理类bean的更新完毕，对域需要依赖其他bean的bean进行更新，重新注入依赖 <br>
>> 
> 所有bean处理完毕后，进行springMVC的处理 <br>
>> HandlerManager@Controller注解的类，即控制器进行处理，如果该方法为@RequestMapping方法，则将uri（映射路径）、该控制器、该方法、该方法参数封装成MappingHandler <br>
>> 将所有映射方法封装成映射处理器后，存入mappingHandlerList <br>
>> 继承Servlet并重写service，得到请求Request的路径，遍历mappingHandlerList并匹配，如果匹配失败，则向Response的write流写入404 <br>
>> 否则交由匹配的handler去处理，将得到的处理结果写入Response <br>
>> 将实现的Servlet交由Tomcat，并处理 <br>

怎么感觉越捋越乱了






