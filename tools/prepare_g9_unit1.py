"""Create original editorial drafts once. Never loaded by Android; never overwrite author files."""
import json
from pathlib import Path
from g9_content_workflow import new_document, validate
from g9_unit1_math import result

OUTPUT = Path(__file__).resolve().parents[1] / "docs/curriculum/g9/authoring/unit1"
def cp(ar, en): return dict(ar=ar, en=en)
def section(kind, ar, en, body_ar, body_en, math="", conditions=None):
    return dict(kind=kind,title=cp(ar,en),body=cp(body_ar,body_en),math=math,conditions=conditions or [])
def example(id, prompt, spec, steps, expression, expected):
    return dict(id=id,prompt=cp(*prompt),verification=spec,verifiedResult=result(spec),
        steps=[dict(math=m,reason=cp(ar,en)) for m,ar,en in steps],check=dict(expression=expression,expected=expected))
def exact(id, prompt, hint, correction, spec, expression, answer):
    return dict(id=id,kind="exact_number",prompt=cp(*prompt),hint=cp(*hint),correction=cp(*correction),
        verification=spec,answer=answer,check=dict(expression=expression,expected=answer))
def choice(id,prompt,hint,correction,spec,options,answer):
    return dict(id=id,kind="choice",prompt=cp(*prompt),hint=cp(*hint),correction=cp(*correction),verification=spec,
        options=[dict(id=str(i),label=cp(ar,en),value=value) for i,(ar,en,value) in enumerate(options)],answer=str(answer))

def nature():
    d=new_document("G9-ALG-U01-L01")
    d['objectives']=[cp("أميّز العدد الصحيح والعدد العادي والعدد العشري والعدد غير العادي.","Distinguish integers, rational, terminating-decimal and irrational numbers."),cp("أختبر انتهاء الكتابة العشرية بعد اختزال الكسر.","Test decimal termination after reducing the fraction.")]
    d['prerequisites']=[cp("قسمة الأعداد الصحيحة واختزال الكسور.","Integer division and equivalent fractions.")]
    d['sections']=[
      section('explanation','ابدأ من معنى العدد','Start with the value','قد تتغير كتابة العدد من كسر إلى كتابة عشرية دون أن تتغير قيمته. نبحث عن خصائص القيمة، لا عن شكل الكتابة فقط.','A fraction and its decimal notation can represent the same value. Classify the value, not just its written form.'),
      section('definition','الأعداد العادية','Rational numbers','العدد العادي يمكن كتابته كحاصل قسمة عددين صحيحين، على ألا يكون المقام صفرًا. كل عدد صحيح عدد عادي، لأننا نستطيع وضعه فوق المقام واحد.','A rational number is a quotient of two integers with nonzero denominator. Every integer is rational because its denominator can be one.','a/b; a,b ∈ Z; b ≠ 0'),
      section('definition','العشري وغير العادي','Terminating and irrational','العدد العشري له كتابة عشرية منتهية، ويمكن كتابته بمقام قوة للعدد عشرة. العدد العادي قد يكون دوريًا غير منتهٍ. العدد غير العادي لا يمكن كتابته ككسر من عددين صحيحين؛ التقريب العشري لا يحوّله إلى عدد عشري منتهٍ.','A terminating decimal has finitely many decimal places and a power-of-ten denominator. A rational can instead repeat indefinitely. An irrational is not an integer quotient; a finite approximation does not change that classification.'),
      section('formula','اختبار الانتهاء','Termination test','اختزل الكسر أولًا. تنتهي كتابته العشرية إذا لم يبقَ في المقام أي عامل أولي غير اثنين أو خمسة. هذا معيار مساعد نبرره بتحويل المقام إلى قوة للعشرة.','Reduce first. Its decimal terminates exactly when the denominator has no prime factors except two and five, because it can then be converted to a power of ten.','d = 2^m × 5^n',[cp('الكسر مختزل؛ المقام موجب؛ الأسّان صحيحان غير سالبين.','Reduced fraction; positive denominator; nonnegative integer exponents.')]),
      section('summary','كيف أصنّف؟','Classification steps','أختزل، وأفحص هل القيمة صحيحة؛ إن لم تكن صحيحة أختبر انتهاء الكتابة العشرية. الكسور الباقية عادية دورية. لا أستنتج أن جذرًا غير عادي من شكله وحده: جذر المربع الكامل صحيح.','Reduce, test for an integer, then test termination. Remaining rational fractions recur. A radical is not automatically irrational: a perfect-square root is an integer.')]
    d['examples']=[
      example('nature-e1',('صنّف الكسر السالب.','Classify the negative fraction.'),dict(operation='classification',numerator=-18,denominator=8),[('-18/8 = -9/4','نقسم الطرفين على اثنين.','Divide both parts by two.'),('-9/4 = -225/100 = -2.25','المقام أربعة؛ نحوله إلى مئة. القيمة عشرية غير صحيحة.','Convert denominator four to one hundred: a terminating noninteger.')],'-18/8','-9/4'),
      example('nature-e2',('هل لهذا الكسر كتابة عشرية منتهية؟','Does this fraction terminate?'),dict(operation='classification',numerator=7,denominator=12),[('gcd(7,12) = 1','الكسر مختزل.','The fraction is reduced.'),('12 = 2^2 × 3','يوجد العامل ثلاثة؛ إذن العدد عادي دوري وليس عشريًا منتهيًا.','Factor three remains, so this rational recurs.')],'7/12','7/12'),
      example('nature-e3',('هل كل جذر عدد غير عادي؟','Is every radical irrational?'),dict(operation='classification',radicand=81),[('9^2 = 81','نتعرف إلى مربع كامل.','Identify a perfect square.'),('√81 = 9','الجذر صحيح، وبالتالي عادي وعشري أيضًا.','The root is an integer, hence also rational and terminating.')],'9**2','81')]
    labels=[('عدد صحيح','Integer','integer'),('عشري منتهٍ غير صحيح','Terminating noninteger','terminating'),('عادي دوري غير منتهٍ','Recurring nonterminating rational','recurring'),('غير عادي','Irrational','irrational')]
    cases=[(6,3,'integer'),(-7,4,'terminating'),(5,6,'recurring'),(13,8,'terminating'),(0,7,'integer'),(10,15,'recurring')]
    qs=[]
    for i,(n,b,category) in enumerate(cases):
        qs.append(choice(f'nature-q{i+1}',(f'اختر الوصف الأدق للقيمة {n}/{b}.',f'Choose the most specific listed description of {n}/{b}.'),('اختزل أولًا، ثم افحص المقام. الأعداد الصحيحة عادية وعشرية أيضًا.','Reduce first, then inspect the denominator. Integers are also rational and terminating.'),
          (f'بعد الاختزال تكون القيمة {category}. وصف «منتهٍ غير صحيح» يستثني الأعداد الصحيحة هنا لتكون الخيارات متمايزة.',f'After reduction the value is {category}. Here “terminating noninteger” excludes integers so options are disjoint.'),dict(operation='classification',numerator=n,denominator=b),labels,labels.index(next(x for x in labels if x[2]==category))))
    for i,n in enumerate([2,49,11]):
        cat='integer' if n==49 else 'irrational'
        qs.append(choice(f'nature-root{i}',(f'صنّف √{n}.',f'Classify √{n}.'),('هل العدد تحت الجذر مربع كامل؟','Is the radicand a perfect square?'),
          ('جذر مربع كامل صحيح؛ جذر عدد صحيح موجب غير مربع كامل غير عادي.','A perfect-square root is an integer; the root of a positive nonsquare integer is irrational.'),dict(operation='classification',radicand=n),labels,0 if cat=='integer' else 3))
    d['questions'],d['checkpoint']=qs[:-1],qs[-1];return d

def common_divisors():
    d=new_document('G9-ALG-U01-L02');d['objectives']=[cp('أحدد القواسم المشتركة والقاسم المشترك الأكبر لعددين موجبين.','Find common divisors and the GCD of positive integers.'),cp('أطبق الطرح المتتالي وخوارزمية إقليدس وأفسر معنى الباقي الأخير غير المعدوم.','Apply subtraction and Euclidean division; explain the last nonzero remainder.')]
    d['prerequisites']=[cp('الضرب والقسمة الصحيحة مع الباقي.','Integer multiplication and division with remainder.')]
    d['sections']=[
      section('explanation','قاسم أم مضاعف؟','Divisor or multiple?','القاسم يقسم العدد دون باقٍ. لإيجاد قواسم مشتركة نبحث عما يقسم العددين معًا؛ لا نبحث عن مضاعفاتهما.','A divisor divides with zero remainder. Common divisors divide both numbers; they are not the multiples of the numbers.'),
      section('definition','القاسم المشترك الأكبر','Greatest common divisor','هو أكبر عدد صحيح موجب يقسم العددين. إذا كان واحدًا فقط فالعددان أوليان فيما بينهما؛ لا يشترط أن يكون كل منهما عددًا أوليًا.','The GCD is the largest positive integer dividing both numbers. GCD one means coprime; the numbers themselves need not each be prime.'),
      section('formula','اختزال المسألة','Reduce the problem','طرح الأصغر من الأكبر لا يغير القواسم المشتركة. بدل تكرار الطرح، نقسم مع الباقي. نكرر على المقسوم عليه والباقي؛ المقسوم عليه في القسمة ذات الباقي صفر هو القاسم المشترك الأكبر.','Subtracting the smaller from the larger preserves common divisors. Euclidean division combines repeated subtraction. Continue with divisor and remainder; the divisor when the remainder becomes zero is the GCD.','a = bq + r; gcd(a,b) = gcd(b,r)',[cp('عددان صحيحان موجبان؛ صفر ≤ الباقي < المقسوم عليه. نتوقف عند باقي صفر ولا نقسم على صفر.','Positive integers; 0 ≤ remainder < divisor. Stop at remainder zero; never divide by zero.')]),
      section('explanation','تحقق من الجواب','Check your result','تحقق أن الجواب يقسم العددين. مجرد إيجاد قاسم مشترك لا يثبت أنه الأكبر؛ تبرر خوارزمية إقليدس أنه الأكبر.','Check that the result divides both inputs. Finding any common divisor does not establish maximality; Euclidean division provides that justification.'),
      section('summary','تذكّر الترتيب','Remember the sequence','الأكبر مقسوم، والأصغر مقسوم عليه، ثم ننتقل إلى المقسوم عليه والباقي. إذا كان الأصغر يقسم الأكبر فالأصغر هو الجواب.','Divide larger by smaller; continue with divisor and remainder. If the smaller divides the larger, it is already the GCD.')]
    d['examples']=[
      example('gcd-e1',('أوجد القاسم المشترك الأكبر للعددين 84 و30.','Find gcd(84,30).'),dict(operation='gcd',a=84,b=30),[('84 = 30 × 2 + 24','الباقي أصغر من ثلاثين.','The remainder is less than thirty.'),('30 = 24 × 1 + 6','نقسم من جديد.','Divide again.'),('24 = 6 × 4 + 0','آخر باقٍ غير معدوم ستة؛ وهو يقسم العددين.','The last nonzero remainder is six and divides both inputs.')],'84-30*2-24','0'),
      example('gcd-e2',('استعمل القواسم مع عددين صغيرين.','Use divisor lists for two small numbers.'),dict(operation='gcd',a=18,b=42),[('18: 1,2,3,6,9,18','نعدد القواسم الموجبة للأول.','List positive divisors of the first.'),('42: 1,2,3,6,7,14,21,42','القواسم المشتركة واحد واثنان وثلاثة وستة؛ الأكبر ستة.','Common divisors are one, two, three and six; the greatest is six.')],'18/6+42/6','10'),
      example('gcd-e3',('هل العددان 14 و25 أوليان فيما بينهما؟','Are 14 and 25 coprime?'),dict(operation='gcd',a=14,b=25),[('25 = 14 + 11; 14 = 11 + 3','نبدأ القسمة الإقليدية.','Begin Euclidean division.'),('11 = 3 × 3 + 2; 3 = 2 + 1; 2 = 1 × 2','نصل إلى واحد. العددان ليسا أوليين منفردين، لكنهما أوليان فيما بينهما.','The GCD is one. Neither input is prime, but they are coprime.')],'25-14','11')]
    pairs=[(36,54,18),(72,120,24),(35,64,1),(96,32,32),(147,63,21),(100,45,5),(81,54,27),(91,65,13),(252,105,21)]
    qs=[exact(f'gcd-q{i+1}',(f'أوجد gcd({a}, {b}).',f'Find gcd({a}, {b}).'),('اقسم مع الباقي، وواصل حتى يصبح الباقي صفرًا.','Use Euclidean division until the remainder is zero.'),
        (f'القاسم المشترك الأكبر هو {g}. يقسم العددين، وهو آخر باقٍ غير معدوم في خوارزمية إقليدس.',f'The GCD is {g}. It divides both inputs and is the last nonzero Euclidean remainder.'),dict(operation='gcd',a=a,b=b),str(g),str(g)) for i,(a,b,g) in enumerate(pairs)]
    d['questions'],d['checkpoint']=qs[:-1],qs[-1];return d

def fractions():
    d=new_document('G9-ALG-U01-L03');d['objectives']=[cp('أختزل كسرًا موجبًا دون تغيير قيمته وأبرر توقف الاختزال.','Reduce a positive fraction without changing its value and justify stopping.')];d['prerequisites']=[cp('القاسم المشترك الأكبر والكسور المتكافئة.','GCD and equivalent fractions.')]
    d['sections']=[section('explanation','قيمة واحدة وكتابات متعددة','One value, multiple forms','يمكن أن يمثل كسران القيمة نفسها. أبسط صورة تساعد على المقارنة، لكنها لا تغيّر القيمة.','Equivalent fractions have equal value. Simplest form helps comparison without changing that value.'),
      section('definition','متى يكون الكسر مختزلًا؟','When is a fraction reduced?','يكون كسر من عددين صحيحين موجبين مختزلًا عندما يكون القاسم المشترك الأكبر للبسط والمقام واحدًا.','A positive-integer fraction is reduced when numerator and denominator have GCD one.'),
      section('formula','اقسم الطرفين معًا','Divide both parts','نقسم البسط والمقام على القاسم المشترك الأكبر نفسه. طرح العدد نفسه من الطرفين لا يحافظ عمومًا على قيمة الكسر.','Divide numerator and denominator by the same GCD. Subtracting the same number from both parts does not generally preserve the fraction.','a/b = (a/g)/(b/g); g = gcd(a,b)',[cp('البسط والمقام صحيحان موجبان؛ القاسم المشترك الأكبر موجب.','Positive integer numerator and denominator; positive GCD.')]),
      section('summary','افحص القيمة وأبسط صورة','Check value and simplest form','تحقق بالضرب التبادلي من تساوي القيمتين، ثم تحقق أن القاسم الجديد واحد. قد يكون الكسر مختزلًا منذ البداية.','Use cross multiplication to check equality, then verify GCD one. A fraction may already be reduced.')]
    cases=[(36,84,'3/7'),(45,105,'3/7'),(28,63,'4/9'),(64,96,'2/3'),(17,24,'17/24'),(75,125,'3/5'),(81,108,'3/4'),(26,39,'2/3'),(56,98,'4/7')]
    from math import gcd
    d['examples']=[]
    for i,(a,b,ans) in enumerate(cases[:3]):
      g=gcd(a,b);d['examples'].append(example(f'fraction-e{i}',(f'اختزل {a}/{b}.',f'Reduce {a}/{b}.'),dict(operation='reduced_fraction',numerator=a,denominator=b),[(f'gcd({a},{b}) = {g}','أوجد أكبر قاسم مشترك.','Find the greatest common divisor.'),(f'{a}/{b} = {ans}','اقسم البسط والمقام عليه. الطرفان الجديدان أوليان فيما بينهما.','Divide both parts by it; the new parts are coprime.')],f'{a}/{b}',ans))
    qs=[]
    for i,(a,b,ans) in enumerate(cases):
      g=gcd(a,b);qs.append(dict(id=f'fraction-q{i}',kind='reduced_fraction',numerator=a,denominator=b,answer=ans,verification=dict(operation='reduced_fraction',numerator=a,denominator=b),prompt=cp(f'اختزل {a}/{b} إلى أبسط صورة.',f'Reduce {a}/{b} to simplest form.'),hint=cp(f'ابحث عن القاسم المشترك الأكبر؛ قيمته {g}.',f'Find the GCD; it is {g}.'),correction=cp(f'نقسم الطرفين على {g} فنحصل على {ans}. القاسم الجديد واحد.',f'Divide both parts by {g} to obtain {ans}; the new GCD is one.')))
    d['questions'],d['checkpoint']=qs[:-1],qs[-1];return d

def roots():
    d=new_document('G9-ALG-U01-L04');d['objectives']=[cp('أحسب الجذر الرئيسي وأميز بينه وبين حل معادلة تربيعية.','Compute the principal root and distinguish it from solving a squared equation.'),cp('أبسّط الجذور وأجمع الحدود المتشابهة وأنطق مقامًا بسيطًا ضمن الشروط.','Simplify radicals, combine like terms and rationalize a simple denominator within the domain.')];d['prerequisites']=[cp('المربعات والضرب والكسور وتوزيع الضرب.','Squares, multiplication, fractions and distributivity.')]
    d['sections']=[section('explanation','الجذر الرئيسي غير سالب','The principal root is nonnegative','جذر عدد غير سالب هو العدد غير السالب الذي مربعه يساوي ذلك العدد. إشارة الجذر تعطي قيمة واحدة؛ حل معادلة يكون مربع المجهول فيها عددًا موجبًا يعطي حلين متعاكسين.','The principal root is the nonnegative number whose square is the radicand. A radical denotes one value; a squared-unknown equation with positive right side has two opposite solutions.'),
      section('definition','المجال والإشارة','Domain and sign','لا يوجد جذر تربيعي حقيقي لعدد سالب. جذر الصفر صفر. عند تربيع عدد سالب ثم أخذ جذره لا نعيد الإشارة السالبة؛ نحصل على قيمته المطلقة.','Negative numbers have no real square root. The root of zero is zero. Squaring a negative then taking the root gives its absolute value, not the negative input.'),
      section('formula','قواعد الجذر','Root identities','نستعمل قاعدة الجداء والقسمة مع أعداد غير سالبة. لا توجد قاعدة مماثلة لتوزيع الجذر على جمع عددين.','Product and quotient identities hold on the nonnegative domain. A square root does not distribute over addition.','(√a)^2 = a; √(x^2) = |x|; √(ab) = √a × √b; √(a/b) = √a/√b',[cp('a,b ≥ 0 في الجداء؛ a ≥ 0 وb > 0 في القسمة؛ x عدد حقيقي.','a,b ≥ 0 for products; a ≥ 0 and b > 0 for quotients; x real.')]),
      section('explanation','أخرج المربع الكامل','Extract a square factor','حلل العدد تحت الجذر إلى مربع كامل مضروب في عدد لا يحتوي عاملًا مربعًا أكبر من واحد. اجمع معاملات الجذور ذات الجزء الجذري نفسه فقط.','Factor out a perfect square, leaving a squarefree radicand. Combine coefficients only when the remaining radical parts agree.'),
      section('formula','مقام دون جذر','Rationalize a denominator','اضرب البسط والمقام في الجذر نفسه؛ هذا ضرب في واحد ولا يغير القيمة. لا يجوز أن يكون المقام صفرًا.','Multiply both numerator and denominator by the same root: multiplying by one preserves the value. The denominator must not be zero.','c/√b = c√b/b',[cp('b > 0؛ والمقام الأصلي غير صفر.','b > 0; original denominator nonzero.')]),
      section('summary','ثلاثة فحوص','Three checks','افحص المجال والإشارة أولًا، ثم استخرج المربعات الكاملة، ثم اجمع المتشابه فقط. أبقِ الجواب دقيقًا ولا تستبدل الجذر بتقريب إلا إذا طُلب ذلك.','Check domain and sign, extract square factors, then combine like terms only. Keep exact answers unless an approximation is requested.')]
    d['examples']=[
      example('root-e1',('بسّط √45.','Simplify √45.'),dict(operation='radical',radicand=45),[('45 = 9 × 5','نستخرج أكبر مربع كامل.','Extract the largest square factor.'),('√45 = 3√5','الخمسة لا تحوي عاملًا مربعًا أكبر من واحد.','Five has no nontrivial square factor.')],'3**2*5','45'),
      example('root-e2',('احسب جذر مربع عدد سالب.','Compute the root of a negative number squared.'),dict(operation='sqrt_square',value=-6),[('(-6)^2 = 36','التربيع يعطي عددًا موجبًا.','Squaring gives a positive value.'),('√36 = 6','الجذر الرئيسي غير سالب؛ ليس سالب ستة.','The principal root is nonnegative, not negative six.')],'6**2','36'),
      example('root-e3',('اجمع √12 و√27.','Add √12 and √27.'),dict(operation='combine_radicals',left=12,right=27,rightSign=1),[('√12 = 2√3; √27 = 3√3','نبسّط الحدين أولًا.','Simplify each term first.'),('2√3 + 3√3 = 5√3','الجزء الجذري نفسه؛ نجمع المعاملين.','The radical parts agree, so add coefficients.')],'2+3','5'),
      example('root-e4',('أنطق مقام 6/√3.','Rationalize 6/√3.'),dict(operation='rationalize',numerator=6,radicand=3),[('6/√3 = 6√3/3','نضرب البسط والمقام في جذر ثلاثة.','Multiply both parts by the root of three.'),('6√3/3 = 2√3','نختزل المعامل، والقيمة محفوظة.','Reduce the coefficient, preserving the value.')],'6/3','2')]
    qs=[]
    for i,n in enumerate([0,121,225]):
      a={0:0,121:11,225:15}[n];qs.append(exact(f'root-perfect{i}',(f'احسب √{n}.',f'Compute √{n}.'),('ابحث عن عدد غير سالب مربعه يساوي العدد تحت الجذر.','Find a nonnegative number whose square is the radicand.'),(f'الجذر الرئيسي هو {a} لأن مربعه {n}.',f'The principal root is {a} because its square is {n}.'),dict(operation='sqrt',radicand=n),str(a),str(a)))
    qs.append(exact('root-sign',('احسب √((-8)^2).','Compute √((-8)^2).'),('الجذر الرئيسي غير سالب.','The principal root is nonnegative.'),('الجواب ثمانية، وهو القيمة المطلقة لسالب ثمانية.','The answer is eight, the absolute value of negative eight.'),dict(operation='sqrt_square',value=-8),'8','8'))
    def radical_question(id,prompt,spec,values,answer,explanation):
      return choice(id,prompt,('استخرج المربعات الكاملة، ثم تحقق من المجال وتكافؤ القيمة.','Extract square factors, then check the domain and equality.'),explanation,spec,[(f'{a}√{b}',f'{a}√{b}',[str(a),b]) for a,b in values],answer)
    qs.extend([
      radical_question('root-simplify',('بسّط √75.','Simplify √75.'),dict(operation='radical',radicand=75),[(5,3),(3,5),(25,3)],0,('خمسة وعشرون مربع كامل؛ نخرج جذره خمسة ويبقى جذر ثلاثة.','Extract the root of twenty-five, leaving five times the root of three.')),
      radical_question('root-add',('بسّط √8 + √18.','Simplify √8 + √18.'),dict(operation='combine_radicals',left=8,right=18,rightSign=1),[(5,2),(1,26),(6,2)],0,('نبسّط إلى معاملين اثنين وثلاثة مع جذر اثنين، فيصبح المعامل خمسة.','The coefficients become two and three with root two, giving coefficient five.')),
      radical_question('root-subtract',('بسّط √50 − √8.','Simplify √50 − √8.'),dict(operation='combine_radicals',left=50,right=8,rightSign=-1),[(7,2),(3,2),(1,42)],1,('بعد التبسيط نطرح اثنين من خمسة؛ يبقى ثلاثة أمثال جذر اثنين.','After simplifying subtract two from five, leaving three times root two.')),
      radical_question('root-denominator',('أنطق مقام 4/√2.','Rationalize 4/√2.'),dict(operation='rationalize',numerator=4,radicand=2),[(4,2),(2,2),(2,4)],1,('نضرب الطرفين في جذر اثنين ثم نقسم أربعة على اثنين.','Multiply both parts by root two, then divide four by two.')),
      exact('root-check',('احسب √(9 + 16).','Compute √(9 + 16).'),('اجمع داخل الجذر أولًا؛ لا توزع الجذر على الجمع.','Add inside the radical first; do not distribute the root over addition.'),('المجموع خمسة وعشرون وجذره خمسة. جمع الجذرين منفصلين يعطي سبعة، وهو جواب لمسألة أخرى.','The sum is twenty-five, whose root is five. Separately adding the roots gives seven, answering a different question.'),dict(operation='sqrt',radicand=25),'5','5')])
    d['questions'],d['checkpoint']=qs[:-1],qs[-1];return d

def documents():
    docs=[nature(),common_divisors(),fractions(),roots()]
    for d in docs:
        d.update(authoringPack='g9-u01-v1',author='EN.MATH original editorial draft prepared with Codex; not an educator signoff',contentRevision='u01-draft-v1',
            sourceInspection=dict(pdfFrom=d['source']['pdfFrom'],pdfTo=d['source']['pdfTo'],evidence='docs/QA/g9-phase4-2026-10-30/source-review.json',scope='ENGINEERING_SOURCE_INSPECTION_NOT_EDUCATOR_REVIEW'))
        for item in d['examples']+d['questions']+[d['checkpoint']]:item['sourcePages']=dict(fromPdf=d['source']['pdfFrom'],toPdf=d['source']['pdfTo'])
        for q in d['questions']+[d['checkpoint']]:
            for old,new in [('terminating','عشري منتهٍ غير صحيح'),('recurring','عادي دوري غير منتهٍ'),('integer','عدد صحيح')]: q['correction']['ar']=q['correction']['ar'].replace(old,new)
        errors=validate(d)
        if errors:raise ValueError((d['source']['lessonId'],errors))
    return docs

if __name__=='__main__':
    OUTPUT.mkdir(parents=True,exist_ok=True)
    for document in documents():
        p=OUTPUT/(document['source']['lessonId']+'.json')
        with p.open('x',encoding='utf-8') as f:json.dump(document,f,ensure_ascii=False,indent=2);f.write('\n')
        print('DRAFT_ONLY '+p.name)
