var stompClient = null;
var userID = null;
var init={'questions':[{'question':'jQuery是什么？','answers':['JavaScript库','CSS库','PHP框架','以上都不是'],'correctAnswer':1},{'question':'找出不同类的一项?','answers':['写字台','沙发','电视','桌布'],'correctAnswer':3},{'question':'国土面积最大的国家是：','answers':['美国','中国','俄罗斯','加拿大'],'correctAnswer':3},{'question':'月亮距离地球多远？','answers':['18万公里','38万公里','100万公里','180万公里'],'correctAnswer':2},{'question':'源氏大招怎么念？','answers':['有基佬拉开我裤链','人鬼合一','我的武士之魂在燃烧','尝尝龙神剑的力量吧'],'correctAnswer':1}]};
var Ownscore = 0;
var OwnTrue = 0;
function startTime(){                     //计时函数
    var today=new Date();
    var h=today.getHours();
    var m=today.getMinutes();
    var s=today.getSeconds();// 在小于10的数字前加一个‘0’
    m=checkTime(m);
    s=checkTime(s);
    document.getElementById('txt').innerHTML=h+":"+m+":"+s;
    t=setTimeout(function(){startTime()},500);
}
function checkTime(i){
    if (i<10){
        i="0" + i;
    }
    return i;
}

function showNum(num){
    if(num < 10){
        return "0" + num;
    }
    else
        return String(num);
}//处理单个数字；
function timeStep() {
    var count = 0;
    var timer = null;
    timer = setInterval(function(){
        count++;
        $("#sec").html( showNum(count % 60) ) ;
        $("#min").html(showNum(parseInt(count / 60) % 60));  //parseInt()方法：将一个字符串，转化成一个整数；
        $("#hou").html(showNum(parseInt(count / 3600))); //处理时分秒；
    }, 1000);
}

//function uncompileStr(_0xe483f5){var _0x3a60b0={'HbtRX':function(_0x1535aa,_0x3ad6cc){return _0x1535aa(_0x3ad6cc);},'YyPPx':function(_0x24dd43,_0x5a00dc){return _0x24dd43-_0x5a00dc;},'EhBXN':function(_0x561455,_0x4106c7){return _0x561455<_0x4106c7;},'lDhaq':function(_0x2aea73,_0x224a9c){return _0x2aea73-_0x224a9c;}};_0xe483f5=_0x3a60b0['HbtRX'](unescape,_0xe483f5);var _0x16033c=String['fromCharCode'](_0x3a60b0['YyPPx'](_0xe483f5['charCodeAt'](0x0),_0xe483f5['length']));for(var _0x12066b=0x1;_0x3a60b0['EhBXN'](_0x12066b,_0xe483f5['length']);_0x12066b++){_0x16033c+=String['fromCharCode'](_0x3a60b0['YyPPx'](_0xe483f5['charCodeAt'](_0x12066b),_0x16033c['charCodeAt'](_0x3a60b0['lDhaq'](_0x12066b,0x1))));}return _0x16033c;}

function connect() {


    //while()
    var url = window.location.search;
    var word = url.substring(url.lastIndexOf('=')+1,url.length);
    userID = word;
    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, onConnected, onError);
    event.preventDefault();
}


function onConnected() {
    stompClient.subscribe("/user/topic/game",onMessageReceived)
    stompClient.send("/app/game.add_user", {}, JSON.stringify({sender:userID,type:'SEARCH'}));

}

function ExitGame(){
    stompClient.disconnect();
    //window.location.href = "http://39.106.105.119:8080/ew/fight.jsp";
    // var index = parent.layer.getFrameIndex(window.name);
    // parent.layer.close(index);
}

function onError(error) {
    window.alert("an unexpected error happened ! please exit");
    stompClient.disconnect();
    //window.location.href = "http://39.106.105.119:8080/ew/fight.jsp";
    // var index = parent.layer.getFrameIndex(window.name);
    // parent.layer.close(index)
}

//点对点通信前缀添加“\app"
function onMessageReceived(payload){
    var message = JSON.parse(payload.body);
    //收到服务器确认搜素匹配玩家
    var chatmsg = message.chatMessage;
    var type = chatmsg.type;
    var code = Number(message.code);
    if (code === 200){
        if(type === 'SEARCH'){
            //收到信息，提交匹配请求
            var chatMessage ={
                type: 'SEARCH',
                sender: userID,
                content: 'apply to Match'
            };
            stompClient.send("/app/game.search",{},JSON.stringify(chatMessage));
            //document.write('<div class="atom-wrapper"> <div style="color: #6fb2e5" class="la-ball-atom la-3x"> <div></div> <div></div> <div></div> <div></div> </div> </div>');

        }else if(type === 'MATCHING')                   //等待匹配比赛
        {
                  var chatMessage1 ={
                    type: 'MATCHING',
                    sender: userID,
                    content: 'query Match results'
                };
                stompClient.send("/app/game.Matching",{},JSON.stringify(chatMessage1));
        }else if (type === 'MATCH'){
            var chatMessage2 ={
                type: 'DO_EXAM',
                sender: userID,
                content: 'Start Exam'
            };
            stompClient.send("/app/game.quest",{},JSON.stringify(chatMessage2));
        }else if (type === 'GQUEST'){
            var chatMessage3 ={
                type: 'DO_EXAM',
                sender: userID,
                content: 'get question'
            };
            var data = chatmsg.content;
            init = JSON.parse(data);
            stompClient.send("/app/game.InGame",{},JSON.stringify(chatMessage3));

        } else if(type === 'DO_EXAM'){
            //TODO 初始化答题界面
            document.title = "开始答题";
            // $("#mainContent").innerHTML = ('<div class="demo"> <div id="quiz-container"></div> </div>');
            timestarter = '<div class="clock" > <span id="hou">00</span>:<span id="min">00</span>:<span id="sec">00</span></div>';
            //$("#mainContent").html();
            //document.body.innerHTML = timestarter+ '<div class="demo"> <div id="quiz-container"></div> </div>';
            //TODO 此处可以添加<button onclick="ExitGame()" class="thoughtbot">退出</button>

            $("body").html('<div class="atom-wrapper"><div class="demo"> <div id="quiz-container"></div> </div></div>'+timestarter);
            timeStep();
            $('#quiz-container').jquizzy({
                questions: init.questions
            });


        }else if(type ==='FINISH_PAIR'){
            //TODO 结算界面
            var data1 = chatmsg.content;
            var reportform = JSON.parse(data1);
            var opponentID = Number(reportform.userid);
            var opponentTrue = Number(reportform.truenum);
            var opponentScore = Number(reportform.score);
            //TODO 用于初始化结算界面
            if(opponentScore > Ownscore){
                //window.alert("结束,失败");
                $("#dialog_main").html(
                    "            <video id='video_play' preload muted autoplay=\"autoplay\" loop=\"loop\" class=\"animationV\">\n" +
                    "                <source src=\"media/scene1.mp4\" type=\"video/mp4\">\n" +
                    "            </video>\n" );
            }else if (opponentScore < Ownscore) {
               // window.alert("结束，胜利")
                $("#dialog_main").html(
                    "            <video id='video_play' preload muted autoplay=\"autoplay\" loop=\"loop\" class=\"animationV\">\n" +
                    "                <source src=\"media/scene2.mp4\" type=\"video/mp4\">\n" +
                    "            </video>\n" );
            }else{
                $("#dialog_main").html(
                    "            <video id='video_play' preload muted autoplay=\"autoplay\" loop=\"loop\" class=\"animationV\">\n" +
                    "                <source src=\"media/scene4.mp4\" type=\"video/mp4\">\n" +
                    "            </video>\n" );
            }
            toggleDialog(true);
        }else if(type === 'FINISH_NOPAIR'){
            window.alert("对方尚未完成,请等待！")
        } else if (type === 'WAIT'){
            var chatMessage4 ={
                type: 'DO_EXAM',
                sender: userID,
                content: '提交结算'
            };
            stompClient.send("/app/game.finish",{},JSON.stringify(chatMessage4));

            var dialogHtml ="<div id=\"dialog-face\" class=\"none\">\n" +
                "</div>\n" +
                "<div id=\"dialog\" class=\"none\">\n" +
                "    <div id=\"dialog-wrapper\">\n" +
                "        <div id='dialog_main' class=\"dialog-content\">\n" +
                "        </div>\n" +
                "        <div class=\"dialog-footer\">\n" +
                "            <button onclick=\"toggleDialog(false)\">关闭</button>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</div>";
            $("body").append(dialogHtml);
        }else if(type ==='OVERTIME'){
            //TODO 直接给出失败界面，超时,给爷死

            var dialogHtml ="<div id=\"dialog-face\" class=\"none\">\n" +
                "</div>\n" +
                "<div id=\"dialog\" class=\"none\">\n" +
                "    <div id=\"dialog-wrapper\">\n" +
                "        <div id='dialog_main' class=\"dialog-content\">\n" +
                "            <video id='video_play' preload muted autoplay=\"autoplay\" loop=\"loop\" class=\"animationV\">\n" +
                "                <source src=\"media/scene3.mp4\" type=\"video/mp4\">\n" +
                "            </video>\n" +
                "        </div>\n" +
                "        <div class=\"dialog-footer\">\n" +
                "            <button onclick=\"toggleDialog(false)\">关闭</button>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</div>";
            $("body").append(dialogHtml);
            toggleDialog(true);
            // setTimeout(function () {
            //     window.close();
            // },5000);
            //window.location.href = "http://39.106.105.119:8080/ew/fight.jsp";
        } else{
            console.log("server send an unrecognized message");
        }
    }else if(code === 201){
        window.alert("该账号已在另外一场游戏中，请勿打扰,")
        //window.location.href = "http://39.106.105.119:8080/ew/fight.jsp";
        // var index = parent.layer.getFrameIndex(window.name);
        // parent.layer.close(index)
        //window.close();
    }

}


(function($) {
    $.fn.jquizzy = function(settings) {
        var defaults = {
            questions: null,
            startImg: 'images/start.gif',
            endText: '已结束!',
            shortURL: null,
            sendResultsURL: "server",
            resultComments: {
                perfect: '你是爱因斯坦么?',
                excellent: '非常优秀!',
                good: '很好，发挥不错!',
                average: '一般般了。',
                bad: '太可怜了！',
                poor: '好可怕啊！',
                worst: '悲痛欲绝！'
            }
        };
        var config = $.extend(defaults, settings);
        if (config.questions === null) {
            $(this).html('<div class="intro-container slide-container"><h2 class="qTitle">Failed to parse questions.</h2></div>');
            return;
        }
        var superContainer = $(this),
        answers = [],
        introFob = '	<div class="intro-container slide-container"><a class="nav-start" href="#">请认真完成测试题。准备好了吗？注意右上角时间哦<br/><br/><span><img src="'+config.startImg+'"></span></a></div>	',
        exitFob = '<div class="results-container slide-container"><div class="question-number">' + config.endText + '</div><div class="result-keeper"></div></div><div class="notice">请选择一个选项！</div><div class="progress-keeper" ><div class="progress"></div></div>',
        contentFob = '',
        questionsIteratorIndex,
        answersIteratorIndex;
        superContainer.addClass('main-quiz-holder');
        for (questionsIteratorIndex = 0; questionsIteratorIndex < config.questions.length; questionsIteratorIndex++) {
            contentFob += '<div class="slide-container"><div class="question-number">' + (questionsIteratorIndex + 1) + '/' + config.questions.length + '</div><div class="question">' + config.questions[questionsIteratorIndex].question + '</div><ul class="answers">';
            for (answersIteratorIndex = 0; answersIteratorIndex < config.questions[questionsIteratorIndex].answers.length; answersIteratorIndex++) {
                contentFob += '<li>' + config.questions[questionsIteratorIndex].answers[answersIteratorIndex] + '</li>';
            }
            contentFob += '</ul><div class="nav-container">';
            if (questionsIteratorIndex !== 0) {
                contentFob += '<div class="prev"><a class="nav-previous" href="#">&lt; 上一题</a></div>';
            }
            if (questionsIteratorIndex < config.questions.length - 1) {
                contentFob += '<div class="next"><a class="nav-next" href="#">下一题 &gt;</a></div>';
            } else {
                contentFob += '<div class="next final"><a class="nav-show-result" href="#">完成</a></div>';
            }
            contentFob += '</div></div>';
            answers.push(config.questions[questionsIteratorIndex].correctAnswer);
        }
        superContainer.html(introFob + contentFob + exitFob);
        var progress = superContainer.find('.progress'),
        progressKeeper = superContainer.find('.progress-keeper'),
        notice = superContainer.find('.notice'),
        progressWidth = progressKeeper.width(),
        userAnswers = [],
        questionLength = config.questions.length,
        slidesList = superContainer.find('.slide-container');
        function checkAnswers() {
            var resultArr = [],
            flag = false;
            for (i = 0; i < answers.length; i++) {
                if (answers[i] == userAnswers[i]) {
                    flag = true;
                } else {
                    flag = false;
                }
                resultArr.push(flag);
            }
            return resultArr;
        }
        function roundReloaded(num, dec) {
            var result = Math.round(num * Math.pow(10, dec)) / Math.pow(10, dec);
            return result;
        }
        function judgeSkills(score) {
            var returnString;
            if (score === 100) return config.resultComments.perfect;
            else if (score > 90) return config.resultComments.excellent;
            else if (score > 70) return config.resultComments.good;
            else if (score > 50) return config.resultComments.average;
            else if (score > 35) return config.resultComments.bad;
            else if (score > 20) return config.resultComments.poor;
            else return config.resultComments.worst;
        }
        progressKeeper.hide();
        notice.hide();
        slidesList.hide().first().fadeIn(500);
        superContainer.find('li').click(function() {
            var thisLi = $(this);
            if (thisLi.hasClass('selected')) {
                thisLi.removeClass('selected');
            } else {
                thisLi.parents('.answers').children('li').removeClass('selected');
                thisLi.addClass('selected');
            }
        });
        superContainer.find('.nav-start').click(function() {
            $(this).parents('.slide-container').fadeOut(500,
            function() {
                $(this).next().fadeIn(500);
                progressKeeper.fadeIn(500);
            });
            return false;
        });
        superContainer.find('.next').click(function() {
            if ($(this).parents('.slide-container').find('li.selected').length === 0) {
                notice.fadeIn(300);
                return false;
            }
            notice.hide();
            $(this).parents('.slide-container').fadeOut(500,
            function() {
                $(this).next().fadeIn(500);
            });
            progress.animate({
                width: progress.width() + Math.round(progressWidth / questionLength)
            },
            500);
            return false;
        });
        superContainer.find('.prev').click(function() {
            notice.hide();
            $(this).parents('.slide-container').fadeOut(500,
            function() {
                $(this).prev().fadeIn(500);
            });
            progress.animate({
                width: progress.width() - Math.round(progressWidth / questionLength)
            },
            500);
            return false;
        });
        superContainer.find('.final').click(function() {
            if ($(this).parents('.slide-container').find('li.selected').length === 0) {
                notice.fadeIn(300);
                return false;
            }
            superContainer.find('li.selected').each(function(index) {
                userAnswers.push($(this).parents('.answers').children('li').index($(this).parents('.answers').find('li.selected')) + 1);
            });

            progressKeeper.hide();
            var results = checkAnswers(),
            resultSet = '',
            trueCount = 0,
            shareButton = '',
            score,
            url;
            if (config.shortURL === null) {
                config.shortURL = window.location
            };
            for (var i = 0,
            toLoopTill = results.length; i < toLoopTill; i++) {
                if (results[i] === true) {
                    trueCount++;
                    isCorrect = true;
                }
                resultSet += '<div class="result-row">' + (results[i] === true ? "<div class='correct'>#"+(i + 1)+"<span></span></div>": "<div class='wrong'>#"+(i + 1)+"<span></span></div>");
                resultSet += '<div class="resultsview-qhover">' + config.questions[i].question;
                resultSet += "<ul>";
                for (answersIteratorIndex = 0; answersIteratorIndex < config.questions[i].answers.length; answersIteratorIndex++) {
                    var classestoAdd = '';
                    var userAnswer = '';
                    if (config.questions[i].correctAnswer == answersIteratorIndex + 1) {
                        classestoAdd += 'right';
                        userAnswer += "正确答案,";
                    }
                    if (userAnswers[i] == answersIteratorIndex + 1) {
                        classestoAdd += ' selected';
                        userAnswer += "你的答案,"
                    }
                    resultSet += '<li class="' + classestoAdd + '">' + userAnswer+ config.questions[i].answers[answersIteratorIndex] + '</li>' ;
                }
                resultSet += '</ul></div></div>';
            }
            score = roundReloaded(trueCount / questionLength * 100, 2);
            if (config.sendResultsURL !== null) {
                var resultform = {
                    "userid" : Number(userID),
                    "score": score,
                    "truenum" : trueCount
                };
                Ownscore = score;
                OwnTrue = trueCount;
                stompClient.send("/app/game.submit",{},JSON.stringify(resultform));
                // $.ajax({
                //     type: 'POST',
                //     url: config.sendResultsURL,
                //     data: '{"answers": [' + collate.join(",") + ']}',
                //     complete: function() {
                //         console.log("OH HAI");
                //     }
                // });
            }

            resultSet = '<h2 class="qTitle">' + judgeSkills(score) + '<br/> 您的分数： ' + score + '</h2>' + shareButton + '<div class="jquizzy-clear"></div>' + resultSet + '<div class="jquizzy-clear"></div>';
            superContainer.find('.result-keeper').html(resultSet).show(500);
            superContainer.find('.resultsview-qhover').hide();
            superContainer.find('.result-row').hover(function() {
                $(this).find('.resultsview-qhover').show();
            },
            function() {
                $(this).find('.resultsview-qhover').hide();
            });
            $(this).parents('.slide-container').fadeOut(500,
            function() {
                $(this).next().fadeIn(500);
            });
            return false;
        });
    };
})(jQuery);