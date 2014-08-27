package mason.utils;


public class StepCounter
{
    int iCount;
    int iFrequency;

    public int count() { return iCount; }

    public StepCounter() { this(100); }

    public StepCounter(int iFrequency)
    {
        iCount = 0;
        this.iFrequency = iFrequency;
    }

    public void increment()
    {
        ++iCount;
        if (iFrequency > 0 && (iCount % iFrequency == 0))
            System.out.print(iCount + " ");
    }

    public void dispose()
    {
        System.out.println(iCount + " lines processed.");
    }
}
